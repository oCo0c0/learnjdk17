/*
 * Copyright (c) 2012, 2021, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package jdk.jfr.internal;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.ReadableByteChannel;
import java.time.Instant;
import java.time.Period;
import java.time.Duration;
import java.util.Comparator;
import java.util.Optional;

import jdk.jfr.internal.SecuritySupport.SafePath;

final class RepositoryChunk {

    static final Comparator<RepositoryChunk> END_TIME_COMPARATOR = new Comparator<RepositoryChunk>() {
        @Override
        public int compare(RepositoryChunk c1, RepositoryChunk c2) {
            return c1.endTime.compareTo(c2.endTime);
        }
    };

    private final SafePath chunkFile;
    private final RandomAccessFile unFinishedRAF;

    private Instant endTime = null; // unfinished
    private Instant startTime;
    private int refCount = 0;
    private long size;

    RepositoryChunk(SafePath path) throws Exception {
        this.chunkFile = path;
        this.unFinishedRAF = SecuritySupport.createRandomAccessFile(chunkFile);
    }

    boolean finish(Instant endTime) {
        try {
            unFinishedRAF.close();
            size = SecuritySupport.getFileSize(chunkFile);
            this.endTime = endTime;
            if (Logger.shouldLog(LogTag.JFR_SYSTEM, LogLevel.DEBUG)) {
                Logger.log(LogTag.JFR_SYSTEM, LogLevel.DEBUG, "Chunk finished: " + chunkFile);
            }
            return true;
        } catch (IOException e) {
            final String reason;
            if (isMissingFile()) {
                reason = "Chunkfile \""+ getFile() + "\" is missing. " +
                         "Data loss might occur from " + getStartTime() + " to " + endTime;
            } else {
                reason = e.getClass().getName();
            }
            Logger.log(LogTag.JFR, LogLevel.ERROR, "Could not finish chunk. " + reason);
            return false;
        }
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant timestamp) {
        this.startTime = timestamp;
    }

    public Instant getEndTime() {
        return endTime;
    }

    private void delete(SafePath f) {
        try {
            SecuritySupport.delete(f);
            if (Logger.shouldLog(LogTag.JFR, LogLevel.DEBUG)) {
                Logger.log(LogTag.JFR, LogLevel.DEBUG, "Repository chunk " + f + " deleted");
            }
        } catch (IOException e) {
            // Probably happens because file is being streamed
            // on Windows where files in use can't be removed.
            if (Logger.shouldLog(LogTag.JFR, LogLevel.DEBUG)) {
                Logger.log(LogTag.JFR, LogLevel.DEBUG, "Repository chunk " + f + " could not be deleted: " + e.getMessage());
            }
            if (f != null) {
                FilePurger.add(f);
            }
        }
    }

    private void destroy() {
        try {
            unFinishedRAF.close();
        } catch (IOException e) {
            if (Logger.shouldLog(LogTag.JFR, LogLevel.ERROR)) {
                Logger.log(LogTag.JFR, LogLevel.ERROR, "Could not close random access file: " + chunkFile.toString() + ". File will not be deleted due to: " + e.getMessage());
            }
        } finally {
            delete(chunkFile);
        }
    }

    public synchronized void use() {
        ++refCount;
        if (Logger.shouldLog(LogTag.JFR_SYSTEM, LogLevel.DEBUG)) {
            Logger.log(LogTag.JFR_SYSTEM, LogLevel.DEBUG, "Use chunk " + toString() + " ref count now " + refCount);
        }
    }

    public synchronized void release() {
        --refCount;
        if (Logger.shouldLog(LogTag.JFR_SYSTEM, LogLevel.DEBUG)) {
            Logger.log(LogTag.JFR_SYSTEM, LogLevel.DEBUG, "Release chunk " + toString() + " ref count now " + refCount);
        }
        if (refCount == 0) {
            destroy();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() {
        boolean destroy = false;
        synchronized (this) {
            if (refCount > 0) {
                destroy = true;
            }
        }
        if (destroy) {
            destroy();
        }
    }

    public long getSize() {
        return size;
    }

    public boolean isFinished() {
        return endTime != null;
    }

    @Override
    public String toString() {
        return chunkFile.toString();
    }

    ReadableByteChannel newChannel() throws IOException {
        if (!isFinished()) {
            throw new IOException("Chunk not finished");
        }
        return ((SecuritySupport.newFileChannelToRead(chunkFile)));
    }

    public boolean inInterval(Instant startTime, Instant endTime) {
        if (startTime != null && getEndTime().isBefore(startTime)) {
            return false;
        }
        if (endTime != null && getStartTime().isAfter(endTime)) {
            return false;
        }
        return true;
    }

    public SafePath getFile() {
        return chunkFile;
    }

    public long getCurrentFileSize() {
        try {
            return SecuritySupport.getFileSize(chunkFile);
        } catch (IOException e) {
            return 0L;
        }
    }

    boolean isMissingFile() {
        try {
            return !SecuritySupport.exists(chunkFile);
        } catch (IOException ioe) {
            return true;
        }
    }
}
