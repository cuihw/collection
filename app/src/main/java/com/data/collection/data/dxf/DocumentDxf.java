package com.data.collection.data.dxf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class DocumentDxf {
    BufferedReader br;
    public DocumentDxf (String filename) {
        File f = new File(filename);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            Read(br);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }



    public void Read(BufferedReader br) {
        try {
            String temp = br.readLine();
            while (temp != null) {
                String code = temp;
                String codedata = br.readLine();
                System.out.println("code = " + code + ", codedata = " + codedata);
                String[] str = new String[] { code.trim(), codedata.trim() };
                if (str[1].equals("HEADER"))
                    ReadHeader();
                if (str[1].equals("ENTITIES")) {
                    ReadEntities(str);
                }
                temp = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ReadEntities(String[] str) {
        try {
            while (!str[1].equals("ENDSEC")) {
                if (str[1].equals("LINE")) {
                    ReadLine();
                } else if (str[1].equals("ARC"))
                    ReadArc();
                else if (str[1].equals("LWPOLYLINE")) {
                    ReadLwpolyline();
                } else if (str[1].equals("TEXT")) {
                    ReadText();
                } else if (str[1].equals("MTEXT")) {
                    ReadMText();
                } else
                    str = ReadPair();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String[] ReadPair() {
        return new String[0];
    }

    private void ReadMText() {

    }

    private void ReadText() {

    }

    private void ReadLwpolyline() {

    }

    private void ReadArc() {

    }

    private void ReadLine() {

    }

    private void ReadHeader() {


    }


}
