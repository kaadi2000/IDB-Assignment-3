package exercise3.impl;

import exercise3.lib.Block;
import exercise3.lib.FixedSizeConverter;
import exercise3.lib.Page;
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;

public class PaxPage<T> implements Page<T> {
    /**
     * The available space for the actual records
     */
    private final int dataSize;

    /**
     * Converter used
     */
    private final FixedSizeConverter<T> converter;

    /**
     * Block storing serialized records
     */
    private final Block data;

    /**
     * Meta-data, indicating whether a slot is
     * used or not
     */
    private final boolean[] slotMask;


    /**
     * The remaining size for storing data
     */
    private int sizeRemaining;

    /**
     * Sizes of the different columns
     */
    private final int[] columnSizes;

    /**
     * Beginning offsets of the mini-pages in the data block
     */
    private final int[] minipageOffsets;

    public PaxPage(int size, FixedSizeConverter<T> converter, int[] columnSizes) {
        this.converter = converter;
        // Calculate the maximum number of records we can store
        int numRecords = (size / converter.getSerializedSize());
        if (numRecords * converter.getSerializedSize() + numRecords > size)
            numRecords--;
        // All slots empty initially
        this.slotMask = new boolean[numRecords];
        Arrays.fill(slotMask, false);
        // Calculate sizes
        this.dataSize = size - slotMask.length;
        this.data = new Block(dataSize);
        this.sizeRemaining = numRecords * converter.getSerializedSize();

        this.columnSizes = columnSizes;

        // Calculate offsets of mini-pages in the page
        minipageOffsets = new int[columnSizes.length];
        minipageOffsets[0] = 0;
        for (int i = 1; i < minipageOffsets.length; i++) {
            minipageOffsets[i] = minipageOffsets[i - 1] + numRecords * columnSizes[i];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(DataInput dataInput) throws IOException {
        sizeRemaining = slotMask.length * getRecordSize();

        // Read free space mask
        int numRecords = 0;
        for (int i = 0; i < slotMask.length; i++) {
            slotMask[i] = dataInput.readBoolean();
            numRecords += slotMask[i] ? 1 : 0;
        }
        sizeRemaining -= (numRecords * getRecordSize());
        // Read data
        dataInput.readFully(data.array, 0, dataSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(DataOutput dataOutput) throws IOException {
        for (boolean b : slotMask) dataOutput.writeBoolean(b);
        dataOutput.write(data.array, data.offset, data.size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFreeSpace() {
        return sizeRemaining;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRecordSize() {
        return converter.getSerializedSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short store(T element) {
        // TODO - DONE
        short id = nextFreeId();
        

        for(int c = 0; c < columnSizes.length; c++) {
            int offset = minipageOffsets[c] + id * columnSizes[c];
            try {
                converter.writeColumn(data.dataOutputStream(offset, offset + columnSizes[c]), element, c);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error while writing column " + c + " to data");
            }
        }
        slotMask[id] = true;
        sizeRemaining -= getRecordSize();
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(short id) {
        // TODO -Done
        if(slotMask[id]){
            slotMask[id] = false;
            sizeRemaining += getRecordSize();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get(short id) {
        // TODO - On it
        if(!slotMask[id]) return null;

        T result = null;
        for(int c = 0; c < columnSizes.length; c++){
            int offset = minipageOffsets[c] + id * columnSizes[c];
            try {
                result = converter.readColumn(data.dataInputStream(offset, offset + columnSizes[c]), result, c);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error while trying to read column " + c);
            }
        }

        return result;
    }

    /**
     * Retrieves the next free slot to use (First Fit)
     *
     * @return the slot to use for a new record
     */
    private short nextFreeId() {
        for (short id = 0; id < slotMask.length; id++) {
            if (!slotMask[id])
                return id;
        }
        throw new RuntimeException("No free ids available");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Short> ids() {
        return new Iterator<Short>() {
            short idx = 0;
            short next = computeNext();

            short computeNext() {
                while (idx < slotMask.length && !slotMask[idx])
                    idx++;
                return idx++;
            }

            @Override
            public boolean hasNext() {
                return next < slotMask.length;
            }

            @Override
            public Short next() {
                short res = next;
                next = computeNext();
                return res;
            }
        };
    }
}