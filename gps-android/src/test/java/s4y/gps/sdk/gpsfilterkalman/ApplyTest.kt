package s4y.gps.sdk.gpsfilterkalman

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import s4y.gps.sdk.GPSUpdate
import s4y.gps.sdk.filters.kalman.GPSFilterKalman

class ApplyTest : Fixtures() {
    @ParameterizedTest(name = "{0}.apply should calculate counts, delays and timestamps")
    @MethodSource("provideAllFilters")
    fun apply_shouldUpdateCountsDelaysAndTimestamps(
        filter: GPSFilterKalman,
        updates: List<GPSUpdate>,
        estimations: Estimations
    ) {
        // Arrange
        filter.reset()
        // Act && Assert
        updates.forEachIndexed { index, update ->
            filter.apply(update)
            assertEquals(estimations[index].dt, filter.transition.dtSec, 0.01)
            assertEquals(estimations[index].update.ts, filter.transition.ts)
            assertEquals(estimations[index].count, index + 1)
        }
    }

    @ParameterizedTest(name = "{0}.apply should modify positions")
    @MethodSource("providePositionFilters")
    fun apply_shouldUpdatePosition(
        filter: GPSFilterKalman,
        updates: List<GPSUpdate>,
        estimations: Estimations
    ) {
        // Arrange
        filter.reset()
        // Act & Assert
        updates.forEachIndexed { index, update ->
            filter.apply(update)
            val expected = estimations[index].update
            val actual = filter.transition
            assertEquals(
                expected.latitude, actual.latitude.degrees, 0.0001,
                "Latitude[${index}]: expected ${expected.latitude}/actual ${actual.longitude.degrees}"
            )
            assertEquals(
                expected.longitude, actual.longitude.degrees, 0.0001,
                "Longitude[${index}]: expected ${expected.longitude}/actual ${actual.longitude.degrees}"
            )
        }
    }

    @Disabled("Velocity filter is not supposed to work yet")
    @ParameterizedTest(name = "{0}.apply should modify positions and velocity")
    @MethodSource("provideVelocityFilters")
    fun apply_shouldUpdateVelocity(
        filter: GPSFilterKalman,
        updates: List<GPSUpdate>,
        estimations: Estimations
    ) {
        // Arrange
        filter.reset()
        // Act & Assert
        updates.forEachIndexed { index, update ->
            filter.apply(update)
            val expected = estimations[index].update
            val actual = filter.transition
            assertEquals(
                expected.latitude, actual.latitude.degrees, 0.0001,
                "Latitude[${index}]: expected ${expected.latitude}/actual ${actual.longitude.degrees}"
            )
            assertEquals(
                expected.longitude, actual.longitude.degrees, 0.0001,
                "Longitude[${index}]: expected ${expected.longitude}/actual ${actual.longitude.degrees}"
            )
        }
    }

}