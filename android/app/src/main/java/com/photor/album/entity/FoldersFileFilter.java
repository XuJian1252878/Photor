package com.photor.album.entity;

import java.io.File;
import java.io.FileFilter;

/**
 * 过滤出是文件夹的文件
 */
public class FoldersFileFilter implements FileFilter {
    @Override
    public boolean accept(File file) {
        return file.isDirectory();
    }
}
