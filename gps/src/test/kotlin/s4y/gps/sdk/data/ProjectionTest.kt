package s4y.gps.sdk.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import s4y.gps.sdk.data.Projection
import s4y.gps.sdk.data.Units
import kotlin.math.sqrt

class ProjectionTest {
    @Test
    fun x_shouldBeProjectedAtEquator() {
        val x = Projection.X.fromModule( 1.0, Units.Bearing.fromDegrees(45.0))
        assertEquals(sqrt(2.0) / 2, x.meters)
    }

    @Test
    fun y_shouldBeProjectedAtEquator() {
        val y = Projection.Y.fromModule(1.0, Units.Bearing.fromDegrees(45.0))
        assertEquals(sqrt(2.0) / 2, y.meters, 1e-7)
    }
}