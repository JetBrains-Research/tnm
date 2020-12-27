import org.junit.Assert
import org.junit.Test
import org.nd4j.linalg.factory.Nd4j
import org.apache.commons.math3.linear.MatrixUtils

class PageRankTests {
    @Test
    fun `Example from paper`() {
        val arrayH = arrayOf(
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0/3, 0.0),
            doubleArrayOf(1.0/2, 0.0, 1.0/2, 1.0/3, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(1.0/2, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 1.0/2, 1.0/3, 0.0, 0.0, 1.0/3, 0.0 ),
            doubleArrayOf(0.0, 0.0, 0.0, 1.0/3, 1.0/3, 0.0, 0.0, 1.0/2 ),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0/3, 0.0, 0.0, 1.0/2 ),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0/3, 1.0, 1.0/3, 0.0  )
        )

        val arrayI = arrayOf(
            doubleArrayOf(1.0),
            doubleArrayOf(0.0),
            doubleArrayOf(0.0),
            doubleArrayOf(0.0),
            doubleArrayOf(0.0),
            doubleArrayOf(0.0),
            doubleArrayOf(0.0),
            doubleArrayOf(0.0)
        )

        val M = Nd4j.create(arrayH)
        var I = Nd4j.create(arrayI)

        for (i in 0 until 61) {
            I = M.mmul(I)
        }

        val expected = arrayOf(
            doubleArrayOf(0.06),
            doubleArrayOf(0.0675),
            doubleArrayOf(0.03),
            doubleArrayOf(0.0675),
            doubleArrayOf(0.0975),
            doubleArrayOf(0.2025),
            doubleArrayOf(0.18),
            doubleArrayOf(0.295)
        )


        for ((expect, result) in expected.zip(I.toDoubleMatrix())) {
            Assert.assertEquals(expect[0], result[0], 0.0001)
        }
    }

    @Test
    fun `apache math`() {
        val arrayH = arrayOf(
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0/3, 0.0),
            doubleArrayOf(1.0/2, 0.0, 1.0/2, 1.0/3, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(1.0/2, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 1.0/2, 1.0/3, 0.0, 0.0, 1.0/3, 0.0 ),
            doubleArrayOf(0.0, 0.0, 0.0, 1.0/3, 1.0/3, 0.0, 0.0, 1.0/2 ),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0/3, 0.0, 0.0, 1.0/2 ),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0/3, 1.0, 1.0/3, 0.0  )
        )

        val M = MatrixUtils.createRealMatrix(arrayH)

        val arrayI = arrayOf(
            doubleArrayOf(1.0),
            doubleArrayOf(0.0),
            doubleArrayOf(0.0),
            doubleArrayOf(0.0),
            doubleArrayOf(0.0),
            doubleArrayOf(0.0),
            doubleArrayOf(0.0),
            doubleArrayOf(0.0)
        )

        var I = MatrixUtils.createRealMatrix(arrayI)


        for (i in 0 until 61) {
            I = M.multiply(I)
        }

        val expected = arrayOf(
            doubleArrayOf(0.06),
            doubleArrayOf(0.0675),
            doubleArrayOf(0.03),
            doubleArrayOf(0.0675),
            doubleArrayOf(0.0975),
            doubleArrayOf(0.2025),
            doubleArrayOf(0.18),
            doubleArrayOf(0.295)
        )


        for ((expect, result) in expected.zip(I.data)) {
            Assert.assertEquals(expect[0], result[0], 0.0001)
        }
    }
}