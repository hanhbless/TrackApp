package com.sunnet.service.recorder;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 * <p/>
 * This class using to manager record voice
 */

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

import com.sunnet.service.util.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class AudioRecorderManager {
    private final static int[] sampleRates = {44100, 22050, 11025, 8000};
    private static int indexSample = 0;

    // Callback to UI
    private IRecorder listenerRecorder;
    private static AudioRecorderManager audioRecorderManager = null;

    public static AudioRecorderManager getInstance(Boolean recordingCompressed) {
        if (audioRecorderManager != null)
            return audioRecorderManager;

        if (recordingCompressed) {
            indexSample = 3;
            audioRecorderManager = new AudioRecorderManager(false,
                    AudioSource.VOICE_CALL,
                    sampleRates[indexSample],
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
        } else {
            audioRecorderManager = new AudioRecorderManager(true,
                    AudioSource.VOICE_CALL,
                    sampleRates[3],
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            /*int i = 0;
            do {
                audioRecorderManager = new AudioRecorderManager(true,
                        AudioSource.MIC,
                        sampleRates[i],
                        AudioFormat.CHANNEL_CONFIGURATION_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                indexSample = i;

            }
            while ((++i < sampleRates.length) & !(audioRecorderManager.getState() == AudioRecorderManager.State.INITIALIZING));*/
        }
        return audioRecorderManager;
    }

    /**
     * INITIALIZING : recorder is initializing;
     * READY : recorder has been initialized, recorder not yet started
     * RECORDING : recording
     * ERROR : reconstruction needed
     * STOPPED: reset needed
     */
    public enum State {
        INITIALIZING, READY, RECORDING, ERROR, STOPPED, PAUSED
    }

    // Using when recorder had started already
    private boolean isRecordred = false;


    public static final boolean RECORDING_UNCOMPRESSED = true;
    public static final boolean RECORDING_COMPRESSED = false;

    // The interval in which the recorded samples are output to the file
    // Used only in uncompressed mode
    private static final int TIMER_INTERVAL = 120;

    // Toggles uncompressed recording on/off; RECORDING_UNCOMPRESSED / RECORDING_COMPRESSED
    private boolean rUncompressed;

    // Recorder used for uncompressed recording
    private AudioRecord audioRecorder = null;

    // Recorder used for compressed recording
    private MediaRecorder mediaRecorder = null;

    // Stores current amplitude (only in uncompressed mode)
    private int cAmplitude = 0;

    private double sumBytes;

    // Output file path
    private String filePath = null;

    // Recorder state; see State
    private State state = State.INITIALIZING;

    // File writer (only in uncompressed mode)
    private RandomAccessFile randomAccessWriter;

    // Number of channels, sample rate, sample size(size in bits), buffer size, audio source, sample size(see AudioFormat)
    private short nChannels;
    private int sRate;
    private short bSamples;
    private int bufferSize;
    private int aSource;
    private int aFormat;

    // Number of frames written to file on each output(only in uncompressed mode)
    private int framePeriod;

    // Buffer for output(only in uncompressed mode)
    private byte[] buffer;

    // Number of bytes written to file after header(only in uncompressed mode)
    // after stop() is called, this size is written to the header/data chunk in the wave file
    private int payloadSize;

    /**
     * Returns the state of the recorder in a RehearsalAudioRecord.State typed object.
     * Useful, as no exceptions are thrown.
     *
     * @return recorder state
     */
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setListenerRecorder(IRecorder listener) {
        this.listenerRecorder = listener;
    }

    /*
    *
    * Method used for recording.
    *
    */
    private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
        public void onPeriodicNotification(AudioRecord recorder) {
            isRecordred = true;
            Log.i("TAG", "hanh record read buffer onPeriodicNotification state: " + state + ", buffer: " + buffer.length);
            // This method updateAnswer read buffer
            audioRecorder.read(buffer, 0, buffer.length); // Fill buffer
            if (state == State.PAUSED || state == State.STOPPED)
                return;

            try {
                randomAccessWriter.write(buffer); // Write buffer to file
                payloadSize += buffer.length;

                sumBytes = 0;
                if (bSamples == 16) {
                    for (int i = 0; i < buffer.length / 2; i++) { // 16bit sample size
                        /*short curSample = getShort(buffer[i * 2], buffer[i * 2 + 1]);
                        if (curSample > cAmplitude) { // Check amplitude
                            cAmplitude = curSample;
                        }*/
                        sumBytes += buffer[i] * buffer[i];
                    }
                } else { // 8bit sample size
                    for (int i = 0; i < buffer.length; i++) {
                        /*if (buffer[i] > cAmplitude) { // Check amplitude
                            cAmplitude = buffer[i];
                        }*/
                        sumBytes += buffer[i] * buffer[i];
                    }
                }
                if (buffer.length > 0) {
                    cAmplitude = (int) Math.round(sumBytes / buffer.length);
                }
                Log.i("TAG", "hanh Amplitude: " + cAmplitude);

            } catch (IOException e) {
                Log.e(AudioRecorderManager.class.getName(), "Error occured in updateListener, recording is aborted");
                //stop();
            }

            if (listenerRecorder != null)
                listenerRecorder.updateAmplitude(cAmplitude);

        }

        public void onMarkerReached(AudioRecord recorder) {
            // NOT USED
            Log.i("TAG", "hanh record read buffer onMarkerReached state: " + state);
        }
    };

    /**
     * Default constructor
     * <p/>
     * Instantiates a new recorder, in case of compressed recording the parameters can be left as 0.
     * In case of errors, no exception is thrown, but the state is set to ERROR
     */
    public AudioRecorderManager(boolean uncompressed, int audioSource, int sampleRate, int channelConfig, int audioFormat) {
        try {
            rUncompressed = uncompressed;
            if (rUncompressed) { // RECORDING_UNCOMPRESSED
                if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
                    bSamples = 16;
                } else {
                    bSamples = 8;
                }

                if (channelConfig == AudioFormat.CHANNEL_CONFIGURATION_MONO) {
                    nChannels = 1;
                } else {
                    nChannels = 2;
                }

                aSource = audioSource;
                sRate = sampleRate;
                aFormat = audioFormat;

                framePeriod = sampleRate * TIMER_INTERVAL / 1000;
                bufferSize = framePeriod * 2 * bSamples * nChannels / 8;
                if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)) { // Check to make sure buffer size is not smaller than the smallest allowed one
                    bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                    // Set frame period and timer interval accordingly
                    framePeriod = bufferSize / (2 * bSamples * nChannels / 8);
                    Log.w(AudioRecorderManager.class.getName(), "Increasing buffer size to " + Integer.toString(bufferSize));
                }

                Log.i("TAG", "hanh buffer constructor: " + bufferSize);
                audioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize);

                if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED)
                    throw new Exception("AudioRecord initialization failed");
                audioRecorder.setRecordPositionUpdateListener(updateListener);
                audioRecorder.setPositionNotificationPeriod(framePeriod);
                Log.i("TAG", "hanh set updateListener AudioListener");
            } else { // RECORDING_COMPRESSED
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(AudioSource.VOICE_CALL);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioChannels(1);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            }
            cAmplitude = 0;
            filePath = null;
            state = State.INITIALIZING;
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e(AudioRecorderManager.class.getName(), e.getMessage());
            } else {
                Log.e(AudioRecorderManager.class.getName(), "Unknown error occured while initializing recording");
            }
            state = State.ERROR;
        }
    }

    /**
     * Sets output file path, call directly after construction/reset.
     *
     * @param 'output file path
     */
    public void setOutputFile(String argPath) {
        Log.i("TAG", "hanh path output: " + argPath);
        try {
            if (state == State.INITIALIZING) {
                filePath = argPath;
                if (!rUncompressed) {
                    mediaRecorder.setOutputFile(filePath);
                }
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e(AudioRecorderManager.class.getName(), e.getMessage());
            } else {
                Log.e(AudioRecorderManager.class.getName(), "Unknown error occured while setting output path");
            }
            state = State.ERROR;
        }
    }

    /**
     * Returns the largest amplitude sampled since the last call to this method.
     *
     * @return returns the largest amplitude since the last call, or 0 when not in recording state.
     */
    public int getMaxAmplitude() {
        if (state == State.RECORDING) {
            if (rUncompressed) {
                int result = cAmplitude;
                cAmplitude = 0;
                return result;
            } else {
                try {
                    return mediaRecorder.getMaxAmplitude();
                } catch (IllegalStateException e) {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }


    /**
     * Prepares the recorder for recording, in case the recorder is not in the INITIALIZING state and the file path was not set
     * the recorder is set to the ERROR state, which makes a reconstruction necessary.
     * In case uncompressed recording is toggled, the header of the wave file is written.
     * In case of an exception, the state is changed to ERROR
     */
    public void prepare() {
        try {
            if (state == State.INITIALIZING) {
                if (rUncompressed) {
                    if ((audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) & (filePath != null)) {
                        // write file header

                        randomAccessWriter = new RandomAccessFile(filePath, "rw");

                        randomAccessWriter.setLength(0); // Set file length to 0, to prevent unexpected behavior in case the file already existed
                        randomAccessWriter.writeBytes("RIFF");
                        randomAccessWriter.writeInt(0); // Final file size not known yet, write 0
                        randomAccessWriter.writeBytes("WAVE");
                        randomAccessWriter.writeBytes("fmt ");
                        randomAccessWriter.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
                        randomAccessWriter.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
                        randomAccessWriter.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
                        randomAccessWriter.writeInt(Integer.reverseBytes(sRate)); // Sample rate
                        randomAccessWriter.writeInt(Integer.reverseBytes(sRate * bSamples * nChannels / 8)); // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
                        randomAccessWriter.writeShort(Short.reverseBytes((short) (nChannels * bSamples / 8))); // Block align, NumberOfChannels*BitsPerSample/8
                        randomAccessWriter.writeShort(Short.reverseBytes(bSamples)); // Bits per sample
                        randomAccessWriter.writeBytes("data");
                        randomAccessWriter.writeInt(0); // Data chunk size not known yet, write 0

                        buffer = new byte[framePeriod * bSamples / 8 * nChannels];
                        state = State.READY;
                    } else {
                        Log.e(AudioRecorderManager.class.getName(), "prepare() method called on uninitialized recorder");
                        state = State.ERROR;
                    }
                } else {
                    mediaRecorder.prepare();
                    state = State.READY;
                }
            } else {
                Log.e(AudioRecorderManager.class.getName(), "prepare() method called on illegal state");
                release();
                state = State.ERROR;
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e(AudioRecorderManager.class.getName(), e.getMessage());
            } else {
                Log.e(AudioRecorderManager.class.getName(), "Unknown error occured in prepare()");
            }
            state = State.ERROR;
        }
    }

    /**
     * Releases the resources associated with this class, and removes the unnecessary files, when necessary
     */
    public void release() {
        isRecordred = false;
        if (state == State.RECORDING || state == State.PAUSED) {
            stop();
        } else {
            if ((state == State.READY) & (rUncompressed)) {
                try {
                    randomAccessWriter.close(); // Remove prepared file
                } catch (IOException e) {
                    Log.e(AudioRecorderManager.class.getName(), "I/O exception occured while closing output file");
                }
                (new File(filePath)).delete();
            }
        }

        if (rUncompressed) {
            if (audioRecorder != null) {
                audioRecorder.release();
                Log.i("TAG", "hanh AudioRecord released");
            }
        } else {
            if (mediaRecorder != null) {
                mediaRecorder.release();
                Log.i("TAG", "hanh MediaRecord released");
            }
        }
    }

    /**
     * Resets the recorder to the INITIALIZING state, as if it was just created.
     * In case the class was in RECORDING state, the recording is stopped.
     * In case of exceptions the class is set to the ERROR state.
     */
    public void reset() {
        isRecordred = false;
        try {
            if (state != State.ERROR) {
                release();
                filePath = null; // Reset file path
                cAmplitude = 0; // Reset amplitude
                if (rUncompressed) {
                    Log.i("TAG", "hanh buffer reset: " + bufferSize);
                    audioRecorder = new AudioRecord(aSource, sRate, nChannels + 1, aFormat, bufferSize);
                    if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED)
                        throw new Exception("AudioRecord initialization failed");
                    audioRecorder.setRecordPositionUpdateListener(updateListener);
                    audioRecorder.setPositionNotificationPeriod(framePeriod);
                } else {
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(AudioSource.VOICE_CALL);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                }
                state = State.INITIALIZING;
            }
        } catch (Exception e) {
            Log.e(AudioRecorderManager.class.getName(), e.getMessage());
            state = State.ERROR;
        }
    }

    /**
     * Starts the recording, and sets the state to RECORDING.
     * Call after prepare().
     */
    public void start() {
        try {
            if (state == State.READY) {
                if (rUncompressed) {
                    payloadSize = 0;
                    audioRecorder.startRecording();
                    audioRecorder.read(buffer, 0, buffer.length);
                } else {
                    mediaRecorder.start();
                }
                state = State.RECORDING;
            } else {
                Log.e(AudioRecorderManager.class.getName(), "start() called on illegal state");
                state = State.ERROR;
            }
        } catch (IllegalStateException e) {

        }
    }

    /**
     * Stops the recording, and sets the state to STOPPED.
     * In case of further usage, a reset is needed.
     * Also finalizes the wave file in case of uncompressed recording.
     */
    public void stop() {
        try {
            if (state == State.RECORDING || state == State.PAUSED) {
                if (rUncompressed) {
                    audioRecorder.stop();
                    Log.i("TAG", "hanh AudioRecord stopped");

                    if (payloadSize == 0) {
                        Utils.deleteFile(filePath);
                        return;
                    }

                    try {
                        randomAccessWriter.seek(4); // Write size to RIFF header
                        randomAccessWriter.writeInt(Integer.reverseBytes(36 + payloadSize));

                        randomAccessWriter.seek(40); // Write size to Subchunk2Size field
                        randomAccessWriter.writeInt(Integer.reverseBytes(payloadSize));

                        randomAccessWriter.close();
                        Log.i("TAG", "hanh path record saved: " + filePath);
                    } catch (IOException e) {
                        Log.e(AudioRecorderManager.class.getName(), "I/O exception occured while closing output file");
                        state = State.ERROR;
                    }
                } else {
                    mediaRecorder.stop();
                    Log.i("TAG", "hanh MediaRecord stopped");
                }
                state = State.STOPPED;
            } else {
                Log.e(AudioRecorderManager.class.getName(), "stop() called on illegal state");
                state = State.ERROR;
            }
        } catch (IllegalStateException ignored) {

        }

    }

    public boolean isRecording() {
        com.sunnet.service.log.Log.i("hanh AudioRecorder: state[" + state + "]");
        return (state == State.RECORDING || (state == State.PAUSED && isRecordred));
    }

    public boolean isInitialRecord() {
        return (state == State.INITIALIZING || state == State.READY);
    }

    public boolean isPauseRecord() {
        return state == State.PAUSED;
    }

    public boolean isRecordred() {
        return isRecordred;
    }

    public void setRecordred(boolean isRecordred) {
        this.isRecordred = isRecordred;
    }

    /*
         *
         * Converts a byte[2] to a short, in LITTLE_ENDIAN format
         *
         */
    private short getShort(byte argB1, byte argB2) {
        return (short) (argB1 | (argB2 << 8));
    }


    // Standard
    public static void mergeFilesWav(List<String> listFileJoins, String fileOutput) {
        int RECORDER_SAMPLE_RATE = sampleRates[indexSample];
        try {
            DataOutputStream amplifyOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileOutput)));
            DataInputStream[] mergeFilesStream = new DataInputStream[listFileJoins.size()];
            long[] sizes = new long[listFileJoins.size()];
            for (int i = 0; i < listFileJoins.size(); i++) {
                File file = new File(listFileJoins.get(i));
                sizes[i] = (file.length() - 44) / 2;
            }
            for (int i = 0; i < listFileJoins.size(); i++) {
                mergeFilesStream[i] = new DataInputStream(new BufferedInputStream(new FileInputStream(listFileJoins.get(i))));
                if (i == listFileJoins.size() - 1) {
                    mergeFilesStream[i].skip(24);
                    byte[] sampleRt = new byte[4];
                    mergeFilesStream[i].read(sampleRt);
                    ByteBuffer bbInt = ByteBuffer.wrap(sampleRt).order(ByteOrder.LITTLE_ENDIAN);
                    RECORDER_SAMPLE_RATE = bbInt.getInt();
                    mergeFilesStream[i].skip(16);
                } else {
                    mergeFilesStream[i].skip(44);
                }
            }

            for (int b = 0; b < listFileJoins.size(); b++) {
                for (int i = 0; i < (int) sizes[b]; i++) {
                    byte[] dataBytes = new byte[2];
                    try {
                        dataBytes[0] = mergeFilesStream[b].readByte();
                        dataBytes[1] = mergeFilesStream[b].readByte();
                    } catch (EOFException e) {
                        amplifyOutputStream.close();
                    }
                    short dataInShort = ByteBuffer.wrap(dataBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    float dataInFloat = (float) dataInShort / 37268.0f;

                    short outputSample = (short) (dataInFloat * 37268.0f);
                    byte[] dataFin = new byte[2];
                    dataFin[0] = (byte) (outputSample & 0xff);
                    dataFin[1] = (byte) ((outputSample >> 8) & 0xff);
                    amplifyOutputStream.write(dataFin, 0, 2);

                }
            }
            amplifyOutputStream.close();
            for (int i = 0; i < listFileJoins.size(); i++) {
                mergeFilesStream[i].close();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        long size = 0;
        try {
            FileInputStream fileSize = new FileInputStream(new File(fileOutput));
            size = fileSize.getChannel().size();
            fileSize.close();
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final int RECORDER_BPP = 16;

        long dataSize = size + 36;
        long byteRate = (RECORDER_BPP * RECORDER_SAMPLE_RATE) / 8;
        long longSampleRate = RECORDER_SAMPLE_RATE;
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (dataSize & 0xff);
        header[5] = (byte) ((dataSize >> 8) & 0xff);
        header[6] = (byte) ((dataSize >> 16) & 0xff);
        header[7] = (byte) ((dataSize >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) 1;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) ((RECORDER_BPP) / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (size & 0xff);
        header[41] = (byte) ((size >> 8) & 0xff);
        header[42] = (byte) ((size >> 16) & 0xff);
        header[43] = (byte) ((size >> 24) & 0xff);
        // out.write(header, 0, 44);

        try {
            RandomAccessFile rFile = new RandomAccessFile(fileOutput, "rw");
            rFile.seek(0);
            rFile.write(header);
            rFile.close();

            // Delete files old
            for (String pathFile : listFileJoins) {
                Utils.deleteFile(pathFile);
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
