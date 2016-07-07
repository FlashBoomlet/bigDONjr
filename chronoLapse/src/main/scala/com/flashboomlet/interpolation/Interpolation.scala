package com.flashboomlet.interpolation

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.linear.CholeskyDecomposition
import org.apache.commons.math3.linear.MatrixUtils.createRealMatrix
import org.apache.commons.math3.linear.QRDecomposition
import org.apache.commons.math3.linear.RealMatrix

/**
  * An object to aid in interpolating data
  */
object Interpolation {

  /**
    * A function to interpolate data
    *
    * @param x the x values
    * @param y the y values
    * @param degree the degree of the polynomial
    * @return a polynomial Function
    */
  def interpolateData(
    x: List[Double],
    y: List[Double],
    degree: Int): PolynomialFunction = {

    val d = degree + 1 // Degree Plus one to get the true size of the interpolation polynomial (0..)
    val emptyMatrix: RealMatrix = createRealMatrix(x.length, d)
    // The Matrix being interpolated is called A
    val a = createNewtonsMethodMatrix(
      x,
      d,
      emptyMatrix)
    // A Transposed
    val at = a.transpose()
    val ys: RealMatrix = createRealMatrix(y.length, 1)
    ys.setColumn(0, y.toArray)
    // A Transposed * A
    val ata: RealMatrix = at.multiply(a)
    // A Transposed * Y
    val aty: RealMatrix = at.multiply(ys)
    val qrDecomposition: QRDecomposition = new QRDecomposition(ata)

    if (qrDecomposition.getSolver.isNonSingular) {
      val c = qrDecomposition.getSolver.solve(aty).getColumn(0)
      new PolynomialFunction(c)
    } else {
      // In the Case of a singular matrix.
      val choleskyDecomposition = new CholeskyDecomposition(ata)
      val c = choleskyDecomposition.getSolver.solve(aty).getColumn(0)
      new PolynomialFunction(c)
    }
  }

  /**
    * Func to Array converts a polynomial function to an array of data points.
    * Used to aid in plotting points
    *
    * @param func the Polynomial Function to Convert to an Array
    * @param time the values to evaluate in the function
    * @return the array of evaluated points
    */
  def funcToArray(func: PolynomialFunction, time: List[Double]): List[Double] = time.map(func.value)

  /**
    * Creates a Newtons Method Matrix in order to interpolate the data in a n degree polynomial
    *
    * @param x the list of x values (ideally a 1..n List)
    * @param degree the degree to which to interpolate
    * @param a a real matrix to store the data for the matrix
    * @return a real matrix filled with the data
    */
  private def createNewtonsMethodMatrix(
    x: List[Double],
    degree: Int,
    a: RealMatrix): RealMatrix = {

    val degrees = Range(0, degree, 1)
    val len = Range(0, x.length, 1)
    len.foreach { i =>
      val row = degrees.map(d =>  Math.pow(x(i), d)).toArray
      a.setRow(i, row)
    }
    a
  }
}
