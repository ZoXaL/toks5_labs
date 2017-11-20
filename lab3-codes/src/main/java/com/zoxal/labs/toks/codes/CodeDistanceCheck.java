package com.zoxal.labs.toks.codes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Insert Desciption vere
 *
 * @author Mike
 * @version 11/12/2017
 */
public class CodeDistanceCheck {
    public static void main(String[] args) {
//        System.out.println(codeDistance(new BigInteger("100101", 2)));
        System.out.println(codeDistance(20, new BigInteger("100101", 2)));

//        System.out.println();
    }

    public static int codeDistance(int codeSize, BigInteger polynom) {
        // 1) получить порождающую матрицу
        // 2) найти порожденное пространства
        // 3) найти минимальное кодовое расстояние
        List<BigInteger> residues = CRCUtils.findSingleErrorResidues(codeSize, polynom);
        List<BigInteger> bornMatrix = new ArrayList<>();
//        CRCUtils.prettyPrintBIList(residues, polynom.bitLength() - 1);
//        System.out.println("====");
        for (int i = 0; i < residues.size(); i++) {
            bornMatrix.add(new BigInteger("1", 2).shiftLeft(i + polynom.bitCount()).add(residues.get(i)));
        }

        List<BigInteger> fullDimension = new ArrayList<>();
        for (int i = 1; i < Math.pow(2, bornMatrix.size()); i++) {
            if (i % 512 == 0) System.out.println(i);
            BigInteger sum = new BigInteger("0", 2);
            BigInteger mask = BigInteger.valueOf(i);
            for (int j = 0; j < mask.bitLength(); j++) {
                if (mask.testBit(j)) {
                    sum = sum.xor(bornMatrix.get(j));
                }
            }
            fullDimension.add(sum);
        }
//        CRCUtils.prettyPrintBIList(fullDimension, bornMatrix.get(0).bitLength());
//        System.out.println("======");

        int codeDistance = polynom.bitLength();
        for (int i = 0; i < fullDimension.size(); i++) {
//            System.out.println("Checking i: "+ i);
            for (int j = i + 1; j < fullDimension.size(); j++) {
                int newCodeDistance = getCodeDistance(fullDimension.get(i), fullDimension.get(j));
                if (codeDistance > newCodeDistance) {
//                    System.out.printf("%s(%d), %s(%d) -> %d\n", fullDimension.get(i).toString(2), i, fullDimension.get(j).toString(2), j, newCodeDistance);
                    codeDistance = newCodeDistance;
                }
            }
            if (i % 512 == 0) System.out.println("Checking i: "+ i);
        }
        return codeDistance;

//        return 0;
    }

    public static BigInteger getSum(List<BigInteger> list, BigInteger mask) {
        BigInteger sum = new BigInteger("0", 2);
        for (int i = 0; i < mask.bitLength(); i++) {
            if (mask.testBit(i)) {
                sum = sum.add(list.get(i));
            }
        }
        return sum;
    }

    public static int getCodeDistance(BigInteger a, BigInteger b) {
        return 5;
//        BigInteger xor = a.xor(b);
//        int codeDistance = 0;
//        for (int i = 0; i < xor.bitLength(); i++) {
//            if (xor.testBit(i)) codeDistance++;
//        }
//        return codeDistance;
    }
}
