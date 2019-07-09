package com.data.collection.data.utils;

import com.data.collection.util.Utils;

import java.util.Arrays;

@SuppressWarnings("unused")
public class Types {

    /*1 = BYTE   2 = ASCII  3 = SHORT 4 = LONG  5 = RATIONAL 6 = SBYTE 7 = UNDEFINED 8 = SSHORT  9 = SLONG 10= SRATIONAL  11= FLOAT  12= DOUBLE*/
    public final static byte[] DATATYPE = {1, 1, 1, 2, 4, 8, 1, 1, 2, 4, 8, 4, 8};

    public static long getWordBoundry(long fp) {
        fp = fp + (4 - (fp % 4));
        return fp;
    }

    public static void arrayInsert(byte[] src, int start, byte[] dest) {
        for (int i = 0; i < dest.length; i++) {
            src[start + i] = dest[i];
        }
    }

    public static double getRational(byte[] buffer, int start, boolean byte_order) {
        double result = 0;
        long num = 1, den = 1;
        if (byte_order) {
            num = getLong(buffer, 0, byte_order);
            den = getLong(buffer, 2, byte_order);
        } else {
            num = getLong(buffer, 2, byte_order);
            den = getLong(buffer, 0, byte_order);
        }
        result = (double) num / den;
//		System.err.println("Rational : "+result);
        return result;
    }

    public static byte[] getRationalAsBytes(double value, boolean byte_order) {
        byte[] result = new byte[8];
        long num = 0, den = 0;
        if (byte_order) {

        }
        return result;
    }

    public static long getLong(byte[] buffer, int start, boolean byte_order) {
//		System.out.println("Long Bytes : ");
//		for(int i=0;i<4;i++){
//			System.out.print(buffer[i]+"\t");
//			}
        long result = 0;
        if (byte_order) {
            result = (((buffer[start + 3] & 0xff) << 24) | ((buffer[start + 2] & 0xff) << 16) | ((buffer[start + 1] & 0xff) << 8) | (buffer[start + 0] & 0xff));
        } else {
            result = (((buffer[start + 0] & 0xff) << 24) | ((buffer[start + 1] & 0xff) << 16) | ((buffer[start + 2] & 0xff) << 8) | (buffer[start + 3] & 0xff));
        }
//		System.out.println("Long : "+result);
        return result;
    }

    //
    public static int getLong(byte[] buffer, int start, int length, boolean byte_order) {
        int result = 0;
        if (byte_order) {
            result = 0;
            for (int i = 0; i < length; i++) {
                result |= (buffer[start + i] & 0xff) << (i * 8);
            }
            //result = (((buffer[start+3]& 0xff) << 24) |((buffer[start+2]& 0xff) << 16) | ((buffer[start+1]& 0xff) << 8) | (buffer[start+0]& 0xff));
        } else {
            for (int i = 0; i < length; i++) {
                result |= (buffer[start + i] & 0xff) << ((length - 1 - i) * 8);
            }
        }
//		System.out.println("Long : "+result);
        return result;
    }


    public static byte[] getLongAsBytes(long value, boolean byte_order) {
        byte[] result = new byte[4];
        if (byte_order) {
            result[0] = (byte) (value & 0xff);
            result[1] = (byte) ((value >> 8) & 0xff);
            result[2] = (byte) ((value >> 16) & 0xff);
            result[3] = (byte) ((value >> 24) & 0xff);
        } else {
            result[3] = (byte) (value & 0xff);
            result[2] = (byte) ((value >> 8) & 0xff);
            result[1] = (byte) ((value >> 16) & 0xff);
            result[0] = (byte) ((value >> 24) & 0xff);
        }
        return result;
    }

    /******************************************************************
     *
     *
     *
     *
     **/
    public static int getShort(byte[] buffer, int start, boolean byte_order) {
        int result = 0;
//		System.out.println("Short Bytes");
//		for(int i=0;i<2;i++){
//			System.out.print(buffer[i]+"\t");
//		}
        if (byte_order) {
            result = (((buffer[start + 1] & 0xff) << 8) | (buffer[start + 0] & 0xff));
        } else {
            result = (((buffer[start + 0] & 0xff) << 8) | (buffer[start + 1] & 0xff));
        }
//		System.out.println("Short : "+result);
        return result;
    }

    public static byte[] getShortAsBytes(int value, boolean byte_order) {
        byte[] result = new byte[2];
        if (byte_order) {
            result[0] = (byte) (value & 0xff);
            result[1] = (byte) ((value >> 8) & 0xff);
        } else {
            result[1] = (byte) (value & 0xff);
            result[0] = (byte) ((value >> 8) & 0xff);
        }
        return result;
    }

    /**********************************************************************
     * Method				source_type		target_type
     * getByte()			byte			short
     * getByteAsBytes()	short			byte
     *
     **/
    public static short getByte(byte[] buffer, int start, boolean byte_order) {
        short result = 0;
//		System.out.println("Byte Bytes "+buffer[start]);
        result = (short) (buffer[start + 0] & 0xff);
//		System.out.println("Byte : "+result);
        return result;
    }

    public static byte[] getByteAsBytes(short value, boolean byte_order) {
        byte[] result = new byte[1];
        result[0] = (byte) (value & 0xff);
        return result;
    }

    /******************************************************************
     *
     *
     *
     *
     **/

    public static String getAscii(byte[] buffer, int start, boolean byte_order) {
        StringBuffer result = new StringBuffer("");
        byte temp = (byte) (buffer[start] & 0x7f);
        while (temp != 0) {
            result.append((char) temp);
            temp = (byte) (buffer[++start] & 0x7f);
        }
        return result.substring(0);
    }

    public static byte[] getAsciiAsBytes(String value, boolean byte_order) {
        byte[] result = value.getBytes();
        byte[] res = new byte[result.length + 1];
        Types.arrayInsert(res, 0, result);
//		System.out.println("String: "+value.length()+"Byte: "+res.length);
//		System.out.println("Ascii: "+res[res.length-1]);
        return res;
    }

    public static double getSRational(byte[] buffer, int start, boolean byte_order) {
        double result = 0;
        int num = 1, den = 1;
        if (byte_order) {
            num = getSLong(buffer, 0, byte_order);
            den = getSLong(buffer, 2, byte_order);
        } else {
            num = getSLong(buffer, 2, byte_order);
            den = getSLong(buffer, 0, byte_order);
        }
        result = (double) num / den;
//		System.err.println("SRational : "+result);
        return result;
    }

    public static byte[] getSRationalAsBytes(double value, boolean byte_order) {
        byte[] result = new byte[8];
        return result;
    }

    public static int getSLong(byte[] buffer, int start, boolean byte_order) {
        int result = 0;
        if (byte_order) {
            result = (((buffer[start + 3] & 0xff) << 24) | ((buffer[start + 2] & 0xff) << 16) |
                    ((buffer[start + 1] & 0xff) << 8) | (buffer[start + 0] & 0xff));
        } else {
            result = (((buffer[start + 0] & 0xff) << 24) | ((buffer[start + 1] & 0xff) << 16) |
                    ((buffer[start + 2] & 0xff) << 8) | (buffer[start + 3] & 0xff));
        }
//		System.err.println("SLong : "+result);
        return result;
    }

    public static int getInt(byte[] buffer, int start, boolean byte_order) {
        int result = 0;
        if (byte_order) {
            result = (((buffer[start + 3] & 0xff) << 24) | ((buffer[start + 2] & 0xff) << 16) |
                    ((buffer[start + 1] & 0xff) << 8) | (buffer[start + 0] & 0xff));
        } else {
            result = (((buffer[start + 0] & 0xff) << 24) | ((buffer[start + 1] & 0xff) << 16) |
                    ((buffer[start + 2] & 0xff) << 8) | (buffer[start + 3] & 0xff));
        }
        return result;
    }

    public static byte[] getSLongAsBytes(int value, boolean byte_order) {
        byte[] result = new byte[4];
        if (byte_order) {
            result[0] = (byte) (value & 0xff);
            result[1] = (byte) (value >> 8 & 0xff);
            result[2] = (byte) (value >> 16 & 0xff);
            result[3] = (byte) (value >> 24 & 0xff);

        } else {
            result[3] = (byte) (value & 0xff);
            result[2] = (byte) (value >> 8 & 0xff);
            result[1] = (byte) (value >> 16 & 0xff);
            result[0] = (byte) (value >> 24 & 0xff);
        }
        return result;
    }

    public static short getSShort(byte[] buffer, int start, boolean byte_order) {
        short result = 0;
        if (byte_order) {
            result = (short) (((buffer[start + 1] & 0xff) << 8) | (buffer[start + 0] & 0xff));
        } else {
            result = (short) (((buffer[start + 0] & 0xff) << 8) | (buffer[start + 1] & 0xff));
        }
//		System.err.println("SShort : "+result);
        return result;
    }

    public static byte[] getSShortAsBytes(short value, boolean byte_order) {
        byte[] result = new byte[2];
        if (byte_order) {
            result[0] = (byte) (value & 0xff);
            result[1] = (byte) (value >> 8 & 0xff);
        } else {
            result[1] = (byte) (value & 0xff);
            result[0] = (byte) (value >> 8 & 0xff);
        }
        return result;
    }

    public static byte getSByte(byte[] buffer, int start, boolean byte_order) {
        byte result = 0;
        result = (byte) (buffer[start + 0] & 0xff);
//		System.err.println("SByte : "+result);
        return result;
    }

    public static byte[] getSByteAsBytes(byte value, boolean byte_order) {
        byte[] result = new byte[1];
        result[0] = value;
        return result;
    }

    public static byte getUndefined(byte[] buffer, int start, boolean byte_order) {
        byte result = 0;
        result = (byte) (buffer[start + 0] & 0xff);
//		System.err.println("Undefined : "+result);
        return result;
    }

    public static byte[] getUndefinedAsBytes(byte value, boolean bytte_order) {
        byte[] result = new byte[1];
        result[0] = value;
        return result;
    }

    public static float getFloat(byte[] buffer, int start, boolean byte_order) {
        float result = 1.0f;
        int value = 0;
        if (byte_order) {
            value = buffer[0] | (buffer[1] << 8) | (buffer[2] << 16) | (buffer[3] << 24);
        } else {
            value = buffer[3] | (buffer[2] << 8) | (buffer[1] << 16) | (buffer[0] << 24);
        }
        result = Float.intBitsToFloat(value);
        return result;
    }

    public static byte[] getFloatAsBytes(float value, boolean byte_order) {
        int temp = Float.floatToRawIntBits(value);
        byte[] result = Types.getSLongAsBytes(temp, byte_order);
        return result;
    }

    public static double bytes2Double(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        System.out.println("bytes2Double long value = " + value);
        return Double.longBitsToDouble(value);
    }

    /**
     * 反转数组
     * @param array
     * @return
     */
    public static byte[] reverseArray(byte[] array){
        byte [] newArray = new byte[array.length];
        for(int i=0; i<newArray.length; i++){
            newArray[i] = array[array.length - i - 1];
        }
        return newArray;
    }

    public static double getDouble(byte[] buffer, int start, boolean byte_order) {
        Double result = 1.0;
        long value = 0;
        System.out.println("buffer = " + Arrays.toString(buffer));

        byte[] doubleBuf = new byte[8];
        System.arraycopy(buffer, start, doubleBuf, 0, 8);

        if (!byte_order) {
            doubleBuf = reverseArray(doubleBuf);
        }

        double doublev = bytes2Double(doubleBuf);
        System.out.println("double v = " + doublev);

        System.out.println("doubleBuf = " + Utils.bytes2hex(doubleBuf));

        //result = Double.longBitsToDouble(value);

        return doublev;
    }

    public static byte[] getDoubleAsBytes(double value, boolean byte_order) {
        long temp = Double.doubleToRawLongBits(value);
        byte[] result = Types.getLongAsBytes(temp, byte_order);
        return result;
    }

    public static Object getObject(byte[] buffer, int start, int type, boolean byte_order) {
        Object result = null;
        switch (type) {
            case 1:
                result = getByte(buffer, start, byte_order);
                break;
            case 2:
                result = getAscii(buffer, start, byte_order);
                break;
            case 3:
                result = getShort(buffer, start, byte_order);
                break;
            case 4:
                result = getLong(buffer, start, byte_order);
                break;
            case 5:
                result = getRational(buffer, start, byte_order);
                break;
            case 6:
                result = getSByte(buffer, start, byte_order);
                break;
            case 7:
                result = getUndefined(buffer, start, byte_order);
                break;
            case 8:
                result = getSShort(buffer, start, byte_order);
                break;
            case 9:
                result = getSLong(buffer, start, byte_order);
                break;
            case 10:
                result = getSRational(buffer, start, byte_order);
                break;
            case 11:
                result = getFloat(buffer, start, byte_order);
                break;
            case 12:
                result = getDouble(buffer, start, byte_order);
                System.err.println("double result = " + result);
                break;
            default:
                System.err.println("Datatype Not Found");
        }

        return result;
    }

    public static byte getBit(int i, byte b) {
        byte mask = 0b00000000;
        switch (i) {
            case 0:
                mask = 0b00000001;
                break;
            case 1:
                mask = 0b00000010;
                break;
            case 2:
                mask = 0b00000100;
                break;
            case 3:
                mask = 0b00001000;
                break;
            case 4:
                mask = 0b00010000;
                break;
            case 5:
                mask = 0b00100000;
                break;
            case 6:
                mask = 0b01000000;
                break;
            case 7:
                mask = (byte) 0b10000000;
                break;
        }
        b &= mask;
        if (b == 0) return 0;
        return 1;
    }

//	public static byte[] getObjectAsBytes(ImageFileField IFF,boolean byte_order){
//		
//		
//		byte[] result = new byte[(int) (IFF.getCount() * Types.DATATYPE[IFF.getDatatype()])];
////		System.out.println("Count : "+IFF.getCount()+"Datatype : "+Types.DATATYPE[IFF.getDatatype()]);
////		System.out.println("Result Size "+result.length);
//		int size = 0;
//		if(IFF.getDatatype() == 2){
//			for(Object val : IFF.getValues()){
//				System.out.println(val);
//				size += ((String)val).length() + 1;
//			}
//			result = new byte[size];
//		}
//		
//		int start=0;
//		byte [] temp = null;
//			for(Object val : IFF.getValues()){
//				switch(IFF.getDatatype()){
//					case 1:
//						temp = Types.getByteAsBytes((short)val, byte_order);
//						break;
//					case 2:
//						size = ((String)val).length();
//						temp = Types.getAsciiAsBytes((String)val, byte_order);
//						break;			
//					case 3:
//						temp = Types.getShortAsBytes((int)val, byte_order);
//						break;				
//					case 4:
//						temp = Types.getLongAsBytes((long)val, byte_order);
//						break;				
//					case 5:
//						temp = Types.getRationalAsBytes((double)val, byte_order);
//						break;				
//					case 6:
//						temp = Types.getSByteAsBytes((byte)val, byte_order);
//						break;				
//					case 7:
//						temp = Types.getUndefinedAsBytes((byte)val, byte_order);
//						break;				
//					case 8:
//						temp = Types.getSShortAsBytes((short)val, byte_order);
//						break;				
//					case 9:
//						temp = Types.getSLongAsBytes((int)val, byte_order);
//						break;				
//					case 10:
//						temp = Types.getSRationalAsBytes((double)val, byte_order);
//						break;				
//					case 11:
//						temp = Types.getFloatAsBytes((float)val, byte_order);
//						break;				
//					case 12:
//						temp = Types.getDoubleAsBytes((double)val, byte_order);
//						break;				
//					default:
//						System.out.println("Datatype not found");				
//				}
//				//System.out.println("result remain : "+(result.length-start)+" temp len :"+(temp.length));
//				Types.arrayInsert(result, start, temp);
//				if(IFF.getDatatype() == 2) start += size;
//				start+= Types.DATATYPE[IFF.getDatatype()];
//			}
//		return result;
//	}
}
