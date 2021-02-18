/*
 * Copyright (C) 2015 Realsil Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.realsil.android.blehub.dfu;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MergeFileManager extends BufferedInputStream {
    public static final int SUB_FILE_TYPE_PATCH_IMAGE = 0;
    public static final int SUB_FILE_TYPE_APP_BANK_0_IMAGE = 1;
    public static final int SUB_FILE_TYPE_APP_BANK_1_IMAGE = 2;
    public static final int SUB_FILE_TYPE_USER_DATA = 3;
    public static final int SUB_FILE_TYPE_PATCH_EXTENSION_IMAGE = 4;
    public static final int SUB_FILE_TYPE_CONFIG_FILE = 5;
    public static final int SUB_FILE_TYPE_EXTERNAL_FLASH_IMAGE = 6;
    private static final String TAG = "MergeFileManager";
    private static final int HEADER_SIZE = 44;
    private static final int SUB_FILE_HEADER_SIZE = 12;
    private static final int CHECKSUM_SIZE = 32;
    private static final short SPECIAL_SIGNATURE = 0x4D47;
    /** Merge BIN file header components */
    private int signature;
    private int sizeOfMergedFile;// Normal file will not reach the end, so do not care long.
    private byte[] checksum;
    private int extension;
    private long subFileIndicator;// Only low four byte is used
    private String filePath;
    private ArrayList<SubFileInfo> subFileArrayList;

    /**
     * Creates the BIN Input Stream.
     * The constructor parses the header of BIN file.
     *
     * @param path the merge file path
     * @throws IOException if the stream is closed or another IOException occurs.
     */
    public MergeFileManager(final String path) throws IOException {
        super(new BufferedInputStream(new FileInputStream(path)));

        if (available() < HEADER_SIZE) {
            throw new IOException("The input file size is less to " + HEADER_SIZE + ", please check!");
        }
        // Save the file path
        filePath = path;

        parseMergeBinFileHeader();
    }

    public static String getTypeString(int type) {
        String typeString = "";
        switch (type) {
            case SUB_FILE_TYPE_PATCH_IMAGE:
                typeString = "0: Patch image (Both MP and OTA)";
                break;
            case SUB_FILE_TYPE_APP_BANK_0_IMAGE:
                typeString = "1: App bank 0 image (Both MP and OTA)";
                break;
            case SUB_FILE_TYPE_APP_BANK_1_IMAGE:
                typeString = "2: App bank 1 image (OTA)";
                break;
            case SUB_FILE_TYPE_USER_DATA:
                typeString = "3: User data (MP)";
                break;
            case SUB_FILE_TYPE_PATCH_EXTENSION_IMAGE:
                typeString = "4: Patch extension image (Both MP and OTA)";
                break;
            case SUB_FILE_TYPE_CONFIG_FILE:
                typeString = "5: Config file (MP)";
                break;
            case SUB_FILE_TYPE_EXTERNAL_FLASH_IMAGE:
                typeString = "6: External Flash image (MP)";
                break;
            default:
                break;
        }

        return typeString;
    }

    private void parseMergeBinFileHeader() throws IOException {
        byte[] headerBufByte = new byte[HEADER_SIZE];
        read(headerBufByte, 0, HEADER_SIZE);
        // Little endian
        signature = (((headerBufByte[1] << 8) & 0xff00) | (headerBufByte[0] & 0xff)) & 0xffff;
        if (signature != SPECIAL_SIGNATURE) {
            throw new IOException("The signature is not right.");
        }
        sizeOfMergedFile = (((headerBufByte[5] << 24) & 0xff000000) | ((headerBufByte[4] << 16) & 0xff0000)
                | ((headerBufByte[3] << 8) & 0xff00) | ((headerBufByte[2] & 0xff))) & 0x00ffffffff;
        checksum = new byte[CHECKSUM_SIZE];
        System.arraycopy(headerBufByte, 6, checksum, 0, CHECKSUM_SIZE);

        extension = (((headerBufByte[39] << 8) & 0xff00) | (headerBufByte[38] & 0xff)) & 0xffff;
        subFileIndicator = (((headerBufByte[43] << 24) & 0xff000000) | ((headerBufByte[42] << 16) & 0xff0000)
                | ((headerBufByte[41] << 8) & 0xff00) | ((headerBufByte[40] & 0xff))) & 0x00ffffffff;

        Log.d(TAG, "parseMergeBinFileHeader, signature: " + String.format("0x%04x", signature) +
                ", sizeOfMergedFile: " + String.format("0x%04x", sizeOfMergedFile) +
                ", extension: " + String.format("0x%04x", extension) +
                ", subFileIndicator: " + String.format("0x%04x", subFileIndicator));

        // Step1: Get the total sub file count.
        int subFileTotalCount = 0;
        long temp = subFileIndicator;
        while (temp != 0) {
            if ((temp & 0x01) != 0) {
                subFileTotalCount++;
            }
            temp = temp >> 1;
        }
        Log.d(TAG, "parseMergeBinFileHeader, subFileTotalCount: " + subFileTotalCount);
        // Step2:
        // parse the sub file
        subFileArrayList = new ArrayList<>();
        temp = subFileIndicator;
        int i = 0;
        // The merge file didn't include the start offset info, we should calculate it.
        int startOffset = HEADER_SIZE + subFileTotalCount * SUB_FILE_HEADER_SIZE;
        while (temp != 0) {
            if ((temp & 0x01) != 0) {
                byte[] subFileBuf = new byte[SUB_FILE_HEADER_SIZE];
                read(subFileBuf, 0, SUB_FILE_HEADER_SIZE);
                SubFileInfo subFileInfo = SubFileInfo.parseData(filePath, i, startOffset, subFileBuf);
                // Update start offset
                startOffset += subFileInfo.size;
                subFileArrayList.add(subFileInfo);
            }

            i++;
            temp = temp >> 1;
        }
        // Show all info
        for (SubFileInfo info : subFileArrayList) {
            Log.d(TAG, info.toString());
        }

        // Close
        close();
    }

    public SubFileInfo getSubFileByType(int type) {
        for (SubFileInfo info : subFileArrayList) {
            if (info.type == type) {
                return info;
            }
        }

        return null;
    }

    public BinInputStream getBinInputStreamFromSpecialType(int type) throws IOException {
        for (SubFileInfo info : subFileArrayList) {
            if (info.type == type) {
                final InputStream is = new FileInputStream(filePath);
                is.skip(info.startAddr);
                return new BinInputStream(is);
            }
        }

        return null;
    }

    public int getSignature() {
        return signature;
    }

    public void setSignature(int signature) {
        this.signature = signature;
    }

    public int getSizeOfMergedFile() {
        return sizeOfMergedFile;
    }

    public void setSizeOfMergedFile(int sizeOfMergedFile) {
        this.sizeOfMergedFile = sizeOfMergedFile;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }

    public int getExtension() {
        return extension;
    }

    public void setExtension(int extension) {
        this.extension = extension;
    }

    public long getSubFileIndicator() {
        return subFileIndicator;
    }

    public void setSubFileIndicator(long subFileIndicator) {
        this.subFileIndicator = subFileIndicator;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public ArrayList<SubFileInfo> getSubFileArrayList() {
        return subFileArrayList;
    }

    public void setSubFileArrayList(ArrayList<SubFileInfo> subFileArrayList) {
        this.subFileArrayList = subFileArrayList;
    }

    public static class SubFileInfo {
        public int type;
        public int startAddr;
        public int downloadAddr;
        public int size;
        public int reserved;
        public String filePath;

        private SubFileInfo(String filePath, int type, int startAddr, int downloadAddr, int size, int reserved) {
            this.filePath = filePath;
            this.type = type;
            this.startAddr = startAddr;
            this.downloadAddr = downloadAddr;
            this.size = size;
            this.reserved = reserved;
        }

        public static SubFileInfo parseData(String filePath, int type, int startAddr, byte[] data) {
            int downloadAddr = (((data[3] << 24) & 0xff000000) | ((data[2] << 16) & 0xff0000)
                    | ((data[1] << 8) & 0xff00) | ((data[0] & 0xff))) & 0x00ffffffff;
            int size = (((data[7] << 24) & 0xff000000) | ((data[6] << 16) & 0xff0000)
                    | ((data[5] << 8) & 0xff00) | ((data[4] & 0xff))) & 0x00ffffffff;
            int reserved = 0;

            return new SubFileInfo(filePath, type, startAddr, downloadAddr, size, reserved);
        }

        public int getSubFileVersion() {
            if (type == SUB_FILE_TYPE_APP_BANK_0_IMAGE
                    || type == SUB_FILE_TYPE_APP_BANK_1_IMAGE
                    || type == SUB_FILE_TYPE_PATCH_IMAGE) {
                final InputStream is;
                try {
                    is = new FileInputStream(filePath);
                    is.skip(startAddr);
                    BinInputStream stream = new BinInputStream(is);
                    stream.close();
                    return stream.binFileVersion();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return -1;
        }

        public String toString() {
            return "SubFileInfo, type: " + type + ", startAddr: " + startAddr
                    + ", downloadAddr: " + downloadAddr
                    + ", size: " + size
                    + ", reserved: " + reserved;
        }
    }
}