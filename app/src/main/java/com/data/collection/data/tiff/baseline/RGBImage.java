package com.data.collection.data.tiff.baseline;

import android.graphics.Bitmap;

import com.data.collection.data.utils.Types;

//import java.awt.image.BufferedImage;
//import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RGBImage extends GrayScaleImage {
    //	SamplesPerPixel
    //	Tag = 277  (115.H)
    //	Type = SHORT
    //	The number of components per pixel. This number is 3 for RGB images, unless
    //	extra samples are present. See the ExtraSamples field for further information.
    protected int samplesPerPixel;

    public RGBImage(File input) throws IOException {
        RandomAccessFile fstream = new RandomAccessFile(input, "rw");
        decode(fstream);
//		image = new BufferedImage(	(int) imageWidth,
//									(int) imageLength,
//									 BufferedImage.TYPE_3BYTE_BGR
//				);
        image = Bitmap.createBitmap((int) imageWidth, (int) imageLength, Bitmap.Config.ARGB_8888);
        if (stripOffsets != null) readStrips(fstream);
        if (tileOffsets != null){
            log.append("readTiles...........");
            readTiles(fstream);
        }
    }

    protected boolean byteOrder = false;

    public boolean decode(RandomAccessFile fstream) throws IOException {
        int version;
        long IFDOffset;

        // Read Header from FileInputStream
        byte[] buffer = new byte[8];
        fstream.read(buffer);

        //read Byte Order
        if (buffer[0] == 0x49 && buffer[0] == buffer[1]) {
            byteOrder = true;
        } else if (buffer[0] == 0x4D && buffer[0] == buffer[1]) {
            byteOrder = false;
        }
        log.append("byteOrder = " + byteOrder);

        //read Version
        version = Types.getShort(buffer, 2, byteOrder);
        log.append("Version = " + version);
        //read IFD Offset
        IFDOffset = Types.getLong(buffer, 4, byteOrder);
        log.append("IFDOffset = " + IFDOffset);

        // Read IFD Contents
        // count stores no. of fields in directory
        int fieldCount = 0;

        fstream.seek(IFDOffset); // 跳转到ifd offset
        buffer = new byte[2];
        fstream.read(buffer);

        fieldCount = Types.getShort(buffer, 0, byteOrder);
        log.append("fieldCount = " + fieldCount);

        long filePointer = fstream.getFilePointer();
        // Read Field Content
        int tag;
        int datatype;
        long valueCount;
        long valueOffset;
        boolean isValue = false;
        int start = 0;

        for (int index = 0; index < fieldCount; index++) {

            buffer = new byte[12];

            fstream.seek(filePointer);
            fstream.read(buffer);
            filePointer = fstream.getFilePointer();

            tag = Types.getShort(buffer, 0, byteOrder);
            datatype = Types.getShort(buffer, 2, byteOrder);
            valueCount = Types.getLong(buffer, 4, byteOrder);
            valueOffset = Types.getLong(buffer, 8, byteOrder);

            if ((valueCount * Types.DATATYPE[datatype]) > 4) {
                fstream.seek(valueOffset);
                buffer = new byte[(int) (valueCount * Types.DATATYPE[datatype])];
                fstream.read(buffer);
                start = 0;
                isValue = false;
            } else {
                isValue = true;
            }

            log.append("index : ==================" + index + ", filePointer = " + filePointer + ", tag = " + tag);
            log.append("IsValue : " + isValue + ", Count : " + valueCount + ", Datatype : " + datatype);


            switch (tag) {
                case 256:
                    imageWidth = valueOffset;
                    log.append("Width : " + valueOffset);
                    break;

                case 257:
                    imageLength = valueOffset;
                    log.append("Length : " + valueOffset);
                    break;

                case 258:
                    if (!isValue) {
                        start = 0;
                        bitsPerSample = new int[3];
                        bitsPerSample[0] = (int) Types.getObject(buffer, start, datatype, byteOrder);
                        log.append("BitsPerSample 0 : " + bitsPerSample[0]);
                    } else {
                        bitsPerSample = new int[1];
                        bitsPerSample[0] = (int) valueOffset;
                    }
                    // bitsPerSample = ( Types.getObject(buffer, datatype, 8, byteOrder)) ;
                    log.append("Bits Per Sample : " + bitsPerSample[0]);
                    break;
                case 259:
                    compression = (int) valueOffset;
                    log.append("Compression : " + valueOffset);
                    break;

                case 262:
                    photometricInterpretation = (int) valueOffset;
                    log.append("Photometric Interpretation : " + valueOffset);
                    break;

                case 273:

                    stripOffsets = new long[(int) valueCount];
                    log.append("Strip Offsets Count :  " + valueCount);
                    for (int i = 0; i < valueCount; i++) {
                        stripOffsets[i] = (long) Types.getObject(buffer, start, datatype, byteOrder);
                        log.append("Offset " + i + " : " + stripOffsets[i]);
                        start += Types.DATATYPE[datatype];
                    }
                    break;

                case 277:
                    samplesPerPixel = (int) valueOffset;
                    log.append("Samples Per Pixel : " + samplesPerPixel);
                    break;
                case 278:
                    rowsPerStrip = (long) valueOffset;
                    log.append("Rows Per Strip : " + valueOffset);
                    break;
                case 279:
                    stripByteCounts = new long[(int) valueCount];
                    log.append("Strip Byte Count : " + valueCount);
                    for (int i = 0; i < valueCount; i++) {
                        stripByteCounts[i] = (long) Types.getObject(buffer, start, datatype, byteOrder);
                        log.append("Strip Byte Count " + i + " : " + stripByteCounts[i]);
                        start += Types.DATATYPE[datatype];
                    }
                    break;
                case 282:
                    xResolution = Types.getRational(buffer, 0, byteOrder);
                    log.append("X Resolution : " + xResolution);
                    break;
                case 283:
                    yResolution = Types.getRational(buffer, 0, byteOrder);
                    log.append("Y Resolution : " + yResolution);
                    break;
                case 284:
                    planerConfiguration = (int) valueOffset;
                    log.append("Planer Configuration : " + planerConfiguration);
                    break;
                case 296:
                    resolutionUnit = valueOffset;
                    log.append("Resolution Unit : " + valueOffset);
                    break;
                case 322:
                    tileWidth = valueOffset;
                    log.append("Tile Width : " + tileWidth);
                    break;
                case 323:
                    tileLength = valueOffset;
                    log.append("Tile Length : " + tileLength);
                    break;
                case 324:
                    tileOffsets = new long[(int) valueCount];
                    for (int i = 0; i < valueCount; i++) {
                        tileOffsets[i] = (long) Types.getObject(buffer, start, datatype, byteOrder);
                        log.append("Offset " + i + " : " + tileOffsets[i]);
                        start += Types.DATATYPE[datatype];
                    }
                    break;
                case 325:
                    tileByteCounts = new long[(int) valueCount];
                    for (int i = 0; i < valueCount; i++) {
                        tileByteCounts[i] = (long) Types.getObject(buffer, start, datatype, byteOrder);
                        log.append("tile Byte Count " + i + " : " + tileByteCounts[i]);
                        start += Types.DATATYPE[datatype];
                    }
                    break;
                default:
                    if (isValue) {
                        String outValue = "";
                        outValue = outValue + valueOffset;
                        log.append("isValue outValue : " + outValue);
                    } else {
                        log.append("Unknown Tag Found : " + tag + " ValueOFFset : " + valueOffset);
                        start = 0;

                        Looper:
                        for (int i = 0; i < valueCount; i++) {
                            String outValue = "";

                            switch (datatype) {
                                case 1: // byte
                                    byte object = (byte) Types.getObject(buffer, start, datatype, byteOrder);
                                    outValue = new StringBuffer(object).toString();
                                    start += Types.DATATYPE[datatype];
                                    break;
                                case 2: // ASCII
                                    String character = (String) Types.getObject(buffer, start, datatype, byteOrder);
                                    outValue = outValue + character;
                                    log.append("outValue : " + outValue);
                                    break Looper;
                                case 3 : // SHORT
                                    Object short11 = Types.getObject(buffer, start, datatype, byteOrder);
                                    outValue = outValue + short11;
                                    start += Types.DATATYPE[datatype];
                                    break;
                                case 12 : // DOUBLE
                                    Object DOUBLE1 = Types.getObject(buffer, start, datatype, byteOrder);
                                    outValue = outValue + DOUBLE1;
                                    start += Types.DATATYPE[datatype];
                                    break;
                            }
                            log.append("outValue : " + outValue);
                        }


                    }
                    break;
            }
        }
        return true;
    }

    public boolean writePixels(RandomAccessFile fstream) {
        int row = 0, col = 0, c = 0;
//		WritableRaster wr = image.getRaster();

        byte[] buffer = new byte[1];
        try {
            for (int offset = 0; offset < stripByteCounts.length; offset++) {
                fstream.seek(stripOffsets[offset]);
                for (int count = 0; count < stripByteCounts[offset]; count++, c++) {
                    if (c == imageLength * imageWidth) break;
                    row = (int) (c / imageWidth);
                    col = (int) (c % imageWidth);
                    //buffer = Types.getByteAsBytes( (byte)wr.getSample(row, col, 0), true);
                    //fstream.write(buffer);
                }
            }
        } catch (Exception e) {
            log.append("imageWidth : " + imageWidth + " imageLength :" + imageLength);
            log.append("row : " + row + " col : " + col + " count : " + c);
            e.printStackTrace();
        }
        return true;
    }

    public boolean readStrips(RandomAccessFile fstream) {
        log.append("Reading Pixel Data From Strips");
        int buffersize = 1;
        int row = 0, col = 0, c = 1;
//		short[][] pixel = new short[(int) imageLength][(int)imageWidth]; //allocate the 2d pixel array
//		WritableRaster wr = image.getRaster();
        try {
            for (int offset = 0; offset < stripOffsets.length; offset++) {
                log.append("Offset : " + offset);

                //move file pointer to address of current strip
                fstream.seek((int) stripOffsets[offset]);

                byte[] buffer = new byte[buffersize];//create buffer

                for (int count = 0; count < stripByteCounts[offset]; count++, c++) {
                    fstream.read(buffer);
                    //if( c % imageWidth == 0) log.append();
                    if (c == imageLength * imageWidth) break;
                    row = (int) (c / imageWidth);
                    col = (int) (c % imageWidth);
                    log.append("" + col + row + "buffer = " + (buffer[0] & 0xff));
//				wr.setSample( col, row, 0,(buffer[0] & 0xff));
                    //System.out.print(pixel[row][col]+" ");
                }
            }
        } catch (Exception e) {
            log.append("imageWidth : " + imageWidth + " imageLength :" + imageLength);
            log.append("row : " + row + " col : " + col + " count : " + c);
            e.printStackTrace();
        }
        return false;
    }

    public boolean readTiles(RandomAccessFile fstream) {

        log.append("Reading Pixels Data From Tiles ");
        long tilesAcross;
        long tilesDown;
        long tilesPerImage;
        long pixelsPerTile;
        //WritableRaster wr = image.getRaster();


        tilesAcross = (imageWidth + tileWidth - 1) / tileWidth;
        tilesDown = (imageLength + tileLength - 1) / tileLength;
        tilesPerImage = tilesAcross * tilesDown;
        pixelsPerTile = tileWidth * tileLength;

        log.append("samplesPerPixel : " + samplesPerPixel);
        log.append("TilesAcross : " + tilesAcross);  // 横向 15 列
        log.append("TilesDown : " + tilesDown);  // 纵向 8 行
        log.append("Tiles Per Image : " + tilesPerImage);  // 图片有多少个tiles
        log.append("Pixels Per Tile : " + pixelsPerTile); // 65636

        long offset = 0;
        int count = 0;
        int ROW = 0;
        int COL = 0;
        try {
            byte[] buffer = new byte[(int) pixelsPerTile * samplesPerPixel];
            int[] pixelData = new int[3];

            for (long row = 0; row < tilesDown; row++) {
                for (long col = 0; col < tilesAcross; col++) {
                    fstream.seek(tileOffsets[(int) offset]);
                    fstream.read(buffer);
                    for (count = 0; count < (pixelsPerTile * samplesPerPixel); count += 3) {
                        ROW = (int) ((count / 3) / (tileWidth));
                        ROW += (row * tileLength);
                        COL = (int) ((count / 3) % (tileWidth));
                        COL += (col * tileWidth);
                        if (ROW >= imageLength || COL >= imageWidth) continue;
                        pixelData[0] = buffer[count];    //RED
                        pixelData[1] = buffer[1 + count]; //GREEN
                        pixelData[2] = buffer[2 + count]; //BLUE
                        int color = Types.getLong(buffer, count, 3, byteOrder);
                        color = color| 0xFF000000;
                        // wr.setPixel( COL, ROW, pixelData );
                        image.setPixel(COL, ROW, color);
                    }
                    offset++;
                }
            }
        } catch (Exception e) {
            log.append("Pixels Per Tile : " + pixelsPerTile);
            log.append("tileWidth : " + tileWidth + " tileLength :" + tileLength);
            log.append("ROW : " + ROW + " COL : " + COL + " Count : " + offset);
            e.printStackTrace();
        }

        return false;
    }
}
