package com.data.collection.module;

import java.util.List;

public class ImageData {
    List<FileMap> files;

    public List<FileMap> getFiles() {
        return files;
    }

    public void setFiles(List<FileMap> files) {
        this.files = files;
    }

    static public class FileMap {
        /*
        {
            "name": "5cec9b223d090.png",
            "url": "http://127.0.0.1/collect/data/uploads/20190528/5cec9b223d090.png"
        }*/
        String name;
        String url;
        String original_name;

        public String getOriginal_name() {
            return original_name;
        }

        public void setOriginal_name(String original_name) {
            this.original_name = original_name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
