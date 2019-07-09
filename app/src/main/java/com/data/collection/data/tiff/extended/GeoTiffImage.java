package com.data.collection.data.tiff.extended;

import android.support.constraint.solver.widgets.Rectangle;

import com.data.collection.data.tiff.baseline.RGBImage;
import com.data.collection.data.utils.Types;

import org.osmdroid.util.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class GeoTiffImage extends RGBImage {
	private double mLatNorth;
	private double mLatSouth;
	private double mLonEast;
	private double mLonWest;

	public GeoTiffImage(File input) throws IOException {
		super(input);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean decode(RandomAccessFile fstream) throws IOException {
		log.append("GeoTiffImage  decode");
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

	@Override
	public Rectangle getBounds() {
		return super.getBounds();
	}


	public double getmLatNorth() {
		return mLatNorth;
	}

	public void setmLatNorth(double mLatNorth) {
		this.mLatNorth = mLatNorth;
		boundingBox.set(mLatNorth, mLonEast, mLatSouth, mLonWest);
	}

	public double getmLatSouth() {
		return mLatSouth;
	}

	public void setmLatSouth(double mLatSouth) {
		this.mLatSouth = mLatSouth;
		boundingBox.set(mLatNorth, mLonEast, mLatSouth, mLonWest);
	}

	public double getmLonEast() {
		return mLonEast;
	}

	public void setmLonEast(double mLonEast) {
		this.mLonEast = mLonEast;
		boundingBox.set(mLatNorth, mLonEast, mLatSouth, mLonWest);
	}

	public double getmLonWest() {
		return mLonWest;
	}


	public void setmLonWest(double mLonWest) {
		this.mLonWest = mLonWest;
		boundingBox.set(mLatNorth, mLonEast, mLatSouth, mLonWest);
	}


	BoundingBox boundingBox = new BoundingBox();

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
}
