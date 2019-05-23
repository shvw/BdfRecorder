package com.biorecorder.bdfrecorder.ads;

import android.os.Environment;

import com.biorecorder.multisignal.edflib.EdfReader;
import com.biorecorder.multisignal.edflib.EdfWriter;
import com.biorecorder.multisignal.recordformat.DataHeader;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveToFileSerialListener implements DataRecordListener {

    private EdfWriter edfWriter;
    private DataFrameJoiner dataFrameJoiner = new DataFrameJoiner();

    public SaveToFileSerialListener() {
        String originalFilename = "header.bdf";
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File originalFile = new File(dir, originalFilename);
        EdfReader originalFileReader = null;
        try {
            originalFileReader = new EdfReader(originalFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        DataHeader header = originalFileReader.getHeader();
        long createTime = System.currentTimeMillis();
        DateFormat dateFormat = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
        String timeStamp = dateFormat.format(new Date(createTime));
        File outputFile = new File(dir, timeStamp+".bdf");
        header.setDurationOfDataRecord(1);
        header.setNumberOfDataRecords(-1);
        try {
            edfWriter = new EdfWriter(outputFile, header);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDataRecordReceived(int[] dataRecord, int dataRecordNumber) {
        int[] resData = new int[dataRecord.length - 1];//remove battery data
        for (int i = 0; i < dataRecord.length-1; i++) {
            resData[i] = dataRecord[i];
        }
        dataFrameJoiner.onDataFrameReceived(resData);
    }

    @Override
    public void onStopRecording() {
        edfWriter.close();
    }

    class DataFrameJoiner{
        private int dataCounter = 0;
        int[] joinedData = new int[200];
        public void onDataFrameReceived(int[] data){
            joinedData[dataCounter] = data[0];
            joinedData[dataCounter+50] = data[1];
            joinedData[dataCounter+100] = data[2];
            joinedData[dataCounter+150] = data[3];
            dataCounter++;
            if(dataCounter == 50){
                dataCounter = 0;
                edfWriter.writeDataRecord(joinedData);
            }
        }
    }
}
