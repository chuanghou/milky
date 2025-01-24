package com.stellariver.milky.demo;

import org.junit.jupiter.api.Test;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

import javax.measure.MetricPrefix;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.*;

public class MeasureTest {


    @Test
    public void measure() {

        Quantity<Speed> speedQuantity = Quantities.getQuantity(10, Units.METRE_PER_SECOND);

        Quantity<Time> timeQuantity = Quantities.getQuantity(100, Units.SECOND);

        Quantity<Length> s = speedQuantity.multiply(timeQuantity).asType(Length.class);

        Quantity<Time> timeQuantity1 = Quantities.getQuantity(10, Units.SECOND);

        Quantity<Speed> m = s.divide(timeQuantity1).asType(Speed.class);

        System.out.printf(m.toString());

        System.out.printf(s.toString());
    }
}
