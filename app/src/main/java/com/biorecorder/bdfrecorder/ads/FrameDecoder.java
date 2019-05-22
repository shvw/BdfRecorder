package com.biorecorder.bdfrecorder.ads;


class FrameDecoder {
    private static final String LOG = "FrameDecoder";
    private static final byte START_FRAME_MARKER = (byte) (0xAA & 0xFF);
    private static final byte MESSAGE_MARKER = (byte) (0xA5 & 0xFF);
    private static final byte STOP_FRAME_MARKER = (byte) (0x55 & 0xFF);
    private int MAX_MESSAGE_SIZE = 7;
    /*******************************************************************
     * these fields we need to restore  data records numbers
     *  from short (sent by ads in 2 bytes) to int
     *******************************************************************/
    private static int SHORT_MAX = 65535; // max value of unsigned short
    private int durationOfShortBlockMs;
    private int previousRecordShortNumber = -1;
    private long previousRecordTime;
    private int startRecordNumber;
    private int shortBlocksCount;
    /***************************************************************/

    private int frameIndex;
    private int frameSize;
    private int rowFrameSizeInByte;
    private int numberOf3ByteSamples;
    private int decodedFrameSizeInInt;
    private byte[] rawFrame;
    private int[] accPrev = new int[3];
    private DataRecordListener dataListener;

    FrameDecoder() {
        durationOfShortBlockMs = 1310700;
        numberOf3ByteSamples = 1;
        rowFrameSizeInByte = 16;
        decodedFrameSizeInInt = 5;
        rawFrame = new byte[Math.max(rowFrameSizeInByte, MAX_MESSAGE_SIZE)];
    }

    /**
     * Frame decoder permits to add only ONE DataListener! So if a new listener added
     * the old one are automatically removed
     */
    public void addDataListener(DataRecordListener l) {
        if (l != null) {
            dataListener = l;
        }
    }

    public void onByteReceived(byte inByte) {
        if (frameIndex == 0 && inByte == START_FRAME_MARKER) {
            rawFrame[frameIndex] = inByte;
            frameIndex++;
        } else if (frameIndex == 1 && inByte == START_FRAME_MARKER) {  //receiving data record
            rawFrame[frameIndex] = inByte;
            frameSize = rowFrameSizeInByte;
            frameIndex++;
        } else if (frameIndex == 1 && inByte == MESSAGE_MARKER) {  //receiving message
            rawFrame[frameIndex] = inByte;
            frameIndex++;
        } else if (frameIndex == 2) {
            rawFrame[frameIndex] = inByte;
            frameIndex++;
            if (rawFrame[1] == MESSAGE_MARKER) {   //message length
                // create new rowFrame with length = message length
                int msg_size = inByte & 0xFF;
                if (msg_size <= MAX_MESSAGE_SIZE) {
                    frameSize = msg_size;
                } else {
                     frameIndex = 0;
                }
            }
        } else if (frameIndex > 2 && frameIndex < (frameSize - 1)) {
            rawFrame[frameIndex] = inByte;
            frameIndex++;
        } else if (frameIndex == (frameSize - 1)) {
            rawFrame[frameIndex] = inByte;
            if (inByte == STOP_FRAME_MARKER) {
                onFrameReceived();
            } else {
                String infoMsg = "Invalid data frame. ";
                if(rawFrame[1] == MESSAGE_MARKER) {
                    infoMsg = "Invalid message frame. ";
                }
                infoMsg = infoMsg + "No stop frame marker. Received byte = " + byteToHexString(inByte) + ". Frame index = " + frameIndex;
            }
            frameIndex = 0;
        } else {
            String infoMsg = "Unrecognized byte received: " + byteToHexString(inByte);
            frameIndex = 0;
        }
    }

    private void onFrameReceived() {
        // Frame = \xAA\xAA... => frame[0] and frame[1] = START_FRAME_MARKER - data
        if (rawFrame[1] == START_FRAME_MARKER) {
            onDataRecordReceived();
        }

    }



    private void onDataRecordReceived() {
        int[] decodedFrame = new int[decodedFrameSizeInInt];
        int rawFrameOffset = 4;
        int decodedFrameOffset = 0;
        for (int i = 0; i < numberOf3ByteSamples; i++) {
            decodedFrame[decodedFrameOffset++] = bytesToSignedInt(rawFrame[rawFrameOffset], rawFrame[rawFrameOffset + 1], rawFrame[rawFrameOffset + 2]) / 2;
            rawFrameOffset += 3;
        }

        if (true) {
            int[] accVal = new int[3];
            int accSum = 0;
            for (int i = 0; i < 3; i++) {
                accVal[i] = bytesToSignedInt(rawFrame[rawFrameOffset], rawFrame[rawFrameOffset + 1]);
                rawFrameOffset += 2;
            }
            if (false) {
                for (int i = 0; i < accVal.length; i++) {
                    accSum += Math.abs(accVal[i] - accPrev[i]);
                    accPrev[i] = accVal[i];
                }
                decodedFrame[decodedFrameOffset++] = accSum;
            } else {
                for (int i = 0; i < accVal.length; i++) {
                    decodedFrame[decodedFrameOffset++] = accVal[i];
                }
            }
        }

        if (true) {
            decodedFrame[decodedFrameOffset++] = bytesToSignedInt(rawFrame[rawFrameOffset], rawFrame[rawFrameOffset + 1]);
            rawFrameOffset += 2;
        }

        int recordShortNumber = bytesToUnsignedInt(rawFrame[2], rawFrame[3]);
        notifyDataListeners(decodedFrame, recordShortNumberToInt(recordShortNumber));
    }


    private int recordShortNumberToInt(int recordShortNumber) {
        long time = System.currentTimeMillis();

        if (previousRecordShortNumber == -1) {
            previousRecordShortNumber = recordShortNumber;
            previousRecordTime = time;
            startRecordNumber = recordShortNumber;
            return 0;
        }
        int recordsDistance = recordShortNumber - previousRecordShortNumber;
        if (recordsDistance <= 0) {
            shortBlocksCount++;
            recordsDistance += SHORT_MAX;
        }
        if (time - previousRecordTime > durationOfShortBlockMs / 2 ) {
            long blocks =  (time - previousRecordTime) / durationOfShortBlockMs;
            long timeRecordsDistance = (time - previousRecordTime) % durationOfShortBlockMs;
            // if recordsDistance big and timeRecordsDistance small
            if (recordsDistance > SHORT_MAX * 2 / 3 && timeRecordsDistance < durationOfShortBlockMs / 3) {
                blocks--;
            }
            // if recordsDistance small and timeRecordsDistance big
            if (recordsDistance < SHORT_MAX / 3 && timeRecordsDistance > durationOfShortBlockMs * 2 / 3) {
                blocks++;
            }

            shortBlocksCount += blocks;
        }

        previousRecordTime = time;
        previousRecordShortNumber = recordShortNumber;
        return shortBlocksCount * SHORT_MAX + recordShortNumber - startRecordNumber;
    }

    private void notifyDataListeners(int[] dataRecord, int recordNumber) {
        dataListener.onDataRecordReceived(dataRecord, recordNumber);

    }

    /* Java int BIG_ENDIAN, Byte order: LITTLE_ENDIAN  */
    private static int bytesToSignedInt(byte... b) {
        switch (b.length) {
            case 1:
                return b[0];
            case 2:
                return (b[1] << 8) | (b[0] & 0xFF);
            case 3:
                return (b[2] << 16) | (b[1] & 0xFF) << 8 | (b[0] & 0xFF);
            default:
                return (b[3] << 24) | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | (b[0] & 0xFF);
        }
    }

    /**
     * Convert given LITTLE_ENDIAN ordered bytes to BIG_ENDIAN 32-bit UNSIGNED int.
     * Available number of input bytes: 4, 3, 2 or 1.
     *
     * @param bytes 4, 3, 2 or 1 bytes (LITTLE_ENDIAN ordered) to be converted to int
     * @return 32-bit UNSIGNED int (BIG_ENDIAN)
     */

    public static int bytesToUnsignedInt(byte... bytes) {
        switch (bytes.length) {
            case 1:
                return (bytes[0] & 0xFF);
            case 2:
                return (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
            case 3:
                return (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
            case 4:
                return (bytes[3] & 0xFF) << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
            default:
                String errMsg = "Wrong «number of bytes» = " + bytes.length +
                        "! Available «number of bytes per int»: 4, 3, 2 or 1.";
                throw new IllegalArgumentException(errMsg);
        }
    }
    private static String byteToHexString(byte b) {
        return String.format("%02X ", b);
    }
}
