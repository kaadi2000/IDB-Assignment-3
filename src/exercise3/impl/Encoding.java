package exercise3.impl;

import java.util.ArrayList;

public class Encoding {
    /**
     * Compresses the passed values using Differential Encoding.
     */
    public static int[] encodeDiff(int[] numbers) {
        // TODO
        if (numbers == null || numbers.length == 0) {
            return new int[0];
        }
        int[] e = new int[numbers.length];
        e[0] = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            e[i] = numbers[i] - numbers[i - 1];
        }
        return e;
    }

    /**
     * Decompresses values previously compressed via Differential Encoding.
     */
    public static int[] decodeDiff(int[] numbers) {
        // TODO
        if (numbers == null || numbers.length == 0) {
            return new int[0];
        }
        int[] d = new int[numbers.length];
        d[0] = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            d[i] = d[i - 1] + numbers[i];
        }
        return d;
    }

    /**
     * Compresses the passed values using Variable Byte Encoding.
     */
    public static byte[] encodeVB(int[] numbers) {
        // TODO
        ArrayList<Byte> list = new ArrayList<>();
        for (int number : numbers) {
            ArrayList<Byte> bytes = new ArrayList<>();
            do {
                byte b = (byte) (number & 0x7F);
                bytes.add(b);
                number >>= 7;
            } while (number > 0);

            for (int i = 0; i < bytes.size(); i++) {
                byte b = bytes.get(i);
                if (i < bytes.size() - 1) {
                    b |= 0x80;
                }
                list.add(b);
            }
        }
        byte[] encoded = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            encoded[i] = list.get(i) ;
        }
        return encoded;
    }

    /**
     * Decompresses values previously compressed via Variable Byte Encoding.
     */
    public static int[] decodeVB(byte[] vbs) {
        // TODO
        ArrayList<Integer> nums = new ArrayList<>();
        int num = 0;
        int a = 0;
        for (byte number : vbs) {
            int unsignedbyte = number & 0xFF;
            int value = unsignedbyte & 0x7F;
            num |= (value) << a;
            if ((unsignedbyte & 0x80) == 0) {
                nums.add(num);
                num = 0;
                a = 0;
            } else {
                a += 7;
            }
        }
        int[] decoded = new int[nums.size()];
        for (int i = 0; i < nums.size(); i++) {
            decoded[i] = nums.get(i);
        }
        return decoded;
    }

    public static void main(String[] args) {
        int[] seq = {1, 7, 56, 134, 256, 268, 384, 472, 512, 648};

        // TODO
        // 1
        int[] eDiff = encodeDiff(seq);
        int esize = eDiff.length * 4;
        System.out.println("Differential Encoding:");
        System.out.print("Encoded: ");
        for (int num : eDiff) {
            System.out.print(num + " ");
        }
        System.out.println("\nSize post differential encoding: " + esize);

        int[] dDiff = decodeDiff(eDiff);
        System.out.print("Decoded: ");
        for (int num : dDiff) {
            System.out.print(num + " ");
        }
        System.out.println();

        //2
        byte[] encodedVB = encodeVB(seq);
        int evbsize = encodedVB.length;
        System.out.println("\nVariable Byte Encoding:");
        System.out.print("Encoded bytes: ");
        for (byte b : encodedVB) {
            System.out.print((b & 0xFF) + " ");
        }
        System.out.println("\nSize post variable byte encoding: " + evbsize);

        int[] VBd = decodeVB(encodedVB);
        System.out.print("Decoded VB: ");
        for (int num : VBd) {
            System.out.print(num + " ");
        }
        System.out.println();

        //3
        byte[] encodedVBdiff = encodeVB(eDiff);
        int VBsizediff = encodedVBdiff.length;
        System.out.println("\nDifferential Encoding + Variable Byte Encoding:");
        System.out.print("Encoded bytes: ");
        for (byte b : encodedVBdiff) {
            System.out.print((b & 0xFF) + " ");
        }
        System.out.println("\nSize post differential encoding and variable byte encoding: " + VBsizediff);
        int[] decodedDiffVB = decodeDiff(decodeVB(encodedVBdiff));
        System.out.print("Decoded Differential + VB: ");
        for (int num : decodedDiffVB) {
            System.out.print(num + " ");
        }
    }
}