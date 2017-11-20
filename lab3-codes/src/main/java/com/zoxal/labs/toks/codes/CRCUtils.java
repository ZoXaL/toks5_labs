package com.zoxal.labs.toks.codes;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Insert Desciption vere
 *
 * @author Mike
 * @version 11/12/2017
 */
public class CRCUtils {
    public static List<BigInteger> findSingleErrorResidues(int codeSize, BigInteger polynom) {
        List<BigInteger> residues = new ArrayList<>();
        for (int i = 0; i < codeSize; i++) {
            BigInteger dividend = new BigInteger("1", 2).shiftLeft(i);
            while (dividend.bitLength() >= polynom.bitLength()) {
                BigInteger polynomTemp = polynom.shiftLeft(dividend.bitLength() - polynom.bitLength());
                dividend = dividend.xor(polynomTemp);
            }
            residues.add(dividend);
        }
        return residues;
    }

    /**
     * true if all double-'1' word residues has more then one '1' bit
     *
     * @param codeSize
     * @param polynom
     * @return
     */
    public static boolean checkDoubleDetectingPolynome(int codeSize, BigInteger polynom) {
        List<BigInteger> residues = findSingleErrorResidues(codeSize, polynom);
        for (int i = 0; i < residues.size(); i++) {
            for (int j = i +1 ; j < residues.size(); j++) {
                BigInteger residuesSum = residues.get(i).xor(residues.get(j));
                if (residuesSum.compareTo(new BigInteger("0")) == 0) return false;
                if (residuesSum.compareTo(polynom) > 0) {
                    residuesSum = residuesSum.xor(polynom);
                }
//                System.out.println(residuesSum.toString(2));
                BigInteger tmp = residuesSum;
                while (!residuesSum.testBit(0)) {
                    residuesSum = residuesSum.shiftRight(1);
                }
                if (residuesSum.bitLength() == 1) {
//                    System.out.println("--- error ---");
//                    System.out.printf("%s (%s): %d + %d = %s + %s\n", residuesSum.toString(2), tmp.toString(2), i, j, residues.get(i).toString(2), residues.get(j).toString(2));
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkDoubleDetectingPolynomes2(int codeSize, BigInteger polynom) {
        List<BigInteger> residues = findSingleErrorResidues(codeSize, polynom);
        if (new HashSet<>(residues).size() != residues.size()) {
            return false;
        }

        for (int i = 0; i < residues.size(); i++) {
            for (int j = i +1 ; j < residues.size(); j++) {
                BigInteger residuesSum = residues.get(i).add(residues.get(j));
                if (residuesSum.compareTo(polynom) > 0) {
                    residuesSum = residuesSum.xor(polynom);
                }
                if (residues.contains(residuesSum)) {
//                    System.out.println("--------");
//                    prettyPrintBIList(residues, polynom.bitLength());
//                    System.out.printf("%s -> %s\n", polynom.toString(2), residuesSum.toString(2));
                    return false;
                }
            }
        }
        System.out.println("Winner: " + polynom.toString(2));
        return true;
    }

    /**
     * Для заданных размеров кода и полинома находит такие полиномы,
     * которые при делении кодовых слов вида x^i(x^j + 1) (101, 1001,
     * 1100, 1010) не дадут в остатке число с одной единицей (степень
     * двойки). Это необхоидимое условие для полиномов, способных отличить
     * двойную ошибку от одиночной. Если какое-то слово из 2 единиц дает
     * одну одну единицу в частном, то такие два вектора ошибок неразличимы.
     *
     * @param codeSize
     * @param polynomLenght
     * @return
     */
    public static List<BigInteger> findPolynomes(int codeSize, int polynomLenght) {
        BigInteger polynom = BigInteger.valueOf(1);
        polynom = polynom.shiftLeft(polynomLenght - 1);
        List<BigInteger> polynomes = new ArrayList<>();
        while (!polynom.testBit(polynomLenght)) {
            if (polynom.testBit(3)) {
//                System.out.println("Checking " + polynom.toString(2));
            }
            if (checkDoubleDetectingPolynome(codeSize, polynom)) {
                polynomes.add(polynom);
            }
            polynom = polynom.add(new BigInteger("1"));
        }
        return polynomes;
    }

    public static void prettyPrintBIList(List<BigInteger> BIList, int maxBILength) {
        for (int i = 0; i < BIList.size(); i++) {
            System.out.printf("%2d -- %0" + maxBILength + "d\n", i, new BigInteger(BIList.get(i).toString(2)));
        }
    }

    public static BigInteger divide(BigInteger a, BigInteger b) {
        while (a.bitLength() >= b.bitLength()) {
            BigInteger polynomTemp = b.shiftLeft(a.bitLength() - b.bitLength());
            a = a.xor(polynomTemp);
        }
        return a;
    }

    public static List<BigInteger> getCyclicResidues(BigInteger a, int aLen, BigInteger b) {
        List<BigInteger> residues = new ArrayList<>();
        for (int i = 0; i < a.bitLength(); i++) {
            residues.add(divide(cyclicShiftLeft(a, aLen, i), b));
        }
        return residues;
    }

    public static BigInteger cyclicShiftLeft(BigInteger a, int length, int count) {
        for (; count > 0; count--) {
            a = cyclicShiftLeft(a, length);
//            System.out.println(a.toString(2));
        }
        return a;
    }

    public static BigInteger cyclicShiftLeft(BigInteger a, int length) {
        return a.testBit(length - 1) ?
                a.shiftLeft(1).setBit(0).clearBit(length) :
                a.shiftLeft(1).clearBit(length);
    }
}
