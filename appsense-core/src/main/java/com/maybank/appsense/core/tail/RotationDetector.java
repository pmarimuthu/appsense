package com.maybank.appsense.core.tail;

import com.maybank.appsense.core.model.FileStatRecord;

public class RotationDetector {

    /** A file is treated as rotated when its inode changed or it is now shorter than the saved offset. */
    public boolean isRotated(FileStatRecord baseline, FileStatRecord current, long savedOffset) {
        return baseline.inode() != current.inode() || current.size() < savedOffset;
    }
}
