package main.java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;

class ProductStockTest{

    private ProductStock stock;

    @BeforeAll
    static void beforeAll() {
        System.out.println(" Starting ProductStock tests ");
    }

    @BeforeEach
    void setUp() {
        // productId, location, initialOnHand, reorderThreshold, maxCapacity
        stock = new ProductStock("P1", "LOC-1", 10, 3, 100);
    }

    @AfterEach
    void setDown() {
        System.out.println(" Test finished");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("All ProductStock tests done ");
    }

    // Constructor tests 

    @Tag("sanity")
    @Test
    void testValidConstruction() {
        assertEquals("P1", stock.getProductId());
        assertEquals("LOC-1", stock.getLocation());
        assertEquals(10, stock.getOnHand());
        assertEquals(0, stock.getReserved());
        assertEquals(3, stock.getReorderThreshold());
        assertEquals(100, stock.getMaxCapacity());
    }
    
    @Tag("regression")
    @Test
    void testInvalidProductId() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProductStock(null, "WH-1", 5, 2, 50)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new ProductStock("   ", "WH-1", 5, 2, 50)
        );
    }
    
    @Tag("regression")
    @Test
    void testInvalidLocation() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProductStock("P1", null, 5, 2, 50)
        );
        assertThrows(IllegalArgumentException.class, () ->
               new ProductStock("P1", "   ", 5, 2, 50)
        );
    }
    @Tag("regression")
    @Test
    void testConstructorNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProductStock("P1", "WH-1", -1, 2, 50)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new ProductStock("P1", "WH-1", 5, -1, 50)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new ProductStock("P1", "WH-1", 5, 2, 0)
        );
    }
    @Tag("regression")
    @Test
    void testConstructorRejectsOnHandGreaterThanCapacity() {
        assertThrows(IllegalArgumentException.class, () ->
                new ProductStock("P1", "WH-1", 60, 2, 50)
        );
    }

    //  Getter tests 
    @Tag("sanity")
    @Test
    void testGetProductId() {
        assertEquals("P1", stock.getProductId());
    }

    @Tag("sanity")
    @Test
    void testGetLocation() {
        assertEquals("LOC-1", stock.getLocation());
    }
    @Tag("sanity")
    @Test
    void testGetOnHand() {
        assertEquals(10, stock.getOnHand());
    }



    //  changeLocation 
    @Tag("sanity")
    @Test
    void testChangeLocationValid() {
        stock.changeLocation("LOC-2");
        assertEquals("LOC-2", stock.getLocation());
    }
    
    @Tag("regression")
    @Test
    void testChangeLocationInvalid() {
        assertThrows(IllegalArgumentException.class, () ->
                stock.changeLocation(null)
        );
        assertThrows(IllegalArgumentException.class, () ->
                stock.changeLocation("   ")
        );
    }

    //  addStock 
    @DisplayName("Testing addStock normal case")
    @Tag("sanity")
    @Test
    void testAddStock() {
        stock.addStock(20);
        assertEquals(30, stock.getOnHand());
    }
    @Tag("regression")
    @Test
    void testAddStockBeyondCapacityThrows() {
        // current onHand = 10, maxCapacity = 100
        assertThrows(IllegalStateException.class, () ->
                stock.addStock(100) // would make 110
        );
    }
    @Tag("regression")
    @Test
    void testAddStockNonPositive() {
        assertThrows(IllegalArgumentException.class, () ->
                stock.addStock(0)
        );
        assertThrows(IllegalArgumentException.class, () ->
                stock.addStock(-5)
        );
    }

    // removeDamaged 
    @Tag("sanity")
    @Test
    void testRemoveDamagedNormal() {
        stock.removeDamaged(3);
        assertEquals(7, stock.getOnHand());
    }
    @Tag("regression")
    @Test
    void testRemoveDamagedMoreThanOnHandThrows() {
        assertThrows(IllegalStateException.class, () ->
               stock.removeDamaged(20)
        );
    }
    @Tag("regression")
    @Test
    void testRemoveDamagedNonPositiveThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                stock.removeDamaged(0)
        );
        assertThrows(IllegalArgumentException.class, () ->
                stock.removeDamaged(-1)
        );
    }

    //  reserve / releaseReservation / shipReserved 
    @Tag("sanity")
    @Test
    void testUpdateReorderThresholdValid(){
        stock.updateReorderThreshold(5);
        assertEquals(5, stock.getReorderThreshold());
    }

    @Tag("regression")
    @Test
    void testUpdateReorderThresholdInvalid(){
        assertThrows(IllegalArgumentException.class, () -> stock.updateReorderThreshold(-1));
        assertThrows(IllegalArgumentException.class, () -> stock.updateReorderThreshold(101));
    }

    @Tag("sanity")
    @Test
    void testUpdateMaxCapacityValidAdjustsThreshold() {
        stock.updateReorderThreshold(80);
        stock.updateMaxCapacity(50);
        assertEquals(50, stock.getMaxCapacity());
        assertEquals(50, stock.getReorderThreshold());
    }

    @Tag("regression")
    @Test
    void testUpdateMaxCapacityNonPositive() {
        assertThrows(IllegalArgumentException.class, () -> stock.updateMaxCapacity(0));
        assertThrows(IllegalArgumentException.class, () -> stock.updateMaxCapacity(-10));
    }

    @Tag("regression")
    @Test
    void testUpdateMaxCapacityLessThanOnHand() {
        assertThrows(IllegalStateException.class, () -> stock.updateMaxCapacity(5));
    }
    @Tag("sanity")
    @Test
    void testReserveNormal() {
        stock.reserve(4);
        assertEquals(4, stock.getReserved());
        assertEquals(6, stock.getAvailable());
    }
    @Tag("regression")
    @Test
    void testReserveMoreThanAvailableThrows() {
        assertThrows(IllegalStateException.class, () ->
                stock.reserve(20)
        );
    }
    @Tag("regression")
    @Test
    void testReserveNonPositiveThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                stock.reserve(0)
        );
        assertThrows(IllegalArgumentException.class, () ->
                stock.reserve(-3)
        );
    }
  
    
    @Tag("sanity")
    @Test
    void testReleaseReservationNormal() {
        stock.reserve(5);
        stock.releaseReservation(3);
        assertEquals(2, stock.getReserved());
        assertEquals(8, stock.getAvailable());
    }
    @Tag("regression")
    @Test
    void testReleaseMoreThanReservedThrows() {
        stock.reserve(4);
        assertThrows(IllegalStateException.class, () ->
                stock.releaseReservation(5)
        );
    }
    @Tag("regression")
    @Test
    void testReleaseReservationNonPositiveThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                stock.releaseReservation(0)
        );
        assertThrows(IllegalArgumentException.class, () ->
                stock.releaseReservation(-1)
        );
    }
    @Tag("sanity")
    @Test
    void testShipReservedNormal() {
        stock.reserve(4);
        stock.shipReserved(4);
        assertEquals(6, stock.getOnHand());
        assertEquals(0, stock.getReserved());
        assertEquals(6, stock.getAvailable());
    }
    @Tag("regression")
    @Test
    void testShipMoreThanReservedThrows() {
        stock.reserve(3);
        assertThrows(IllegalStateException.class, () ->
                stock.shipReserved(4)
        );
    }
    @Tag("regression")
    @Test
    void testShipReservedNonPositiveThrows() {
        assertThrows(IllegalArgumentException.class, () ->stock.shipReserved(0)
        );
        assertThrows(IllegalArgumentException.class, () ->stock.shipReserved(-1)
        );
    }

    //  isReorderNeeded 
    @Tag("sanity")
    @Test
    void testIsReorderNeededTrueWhenBelowThreshold() {
       
        stock.reserve(8);
        assertTrue(stock.isReorderNeeded());
    }
    @Tag("regression")
    @Test
    void testIsReorderNeededFalseWhenAboveThreshold() {
        assertFalse(stock.isReorderNeeded()); 
    }

    // updateReorderThreshold / updateMaxCapacity
    
   
    @Tag("sanity")
    @Test
    void testUpdateMaxCapacityValidAlsoAdjustsThresholdIfNeeded() {
        stock.updateReorderThreshold(80);
        stock.updateMaxCapacity(50);
        assertEquals(50, stock.getMaxCapacity());
      
        assertEquals(50, stock.getReorderThreshold());
    }
    @Tag("regression")
    @Test
    void testUpdateMaxCapacityNonPositiveThrows() {
        assertThrows(IllegalArgumentException.class, () ->stock.updateMaxCapacity(0)
        );
        assertThrows(IllegalArgumentException.class, () ->stock.updateMaxCapacity(-10)
        );
    }
    
    @Tag("regression")
    @Test
    void testUpdateMaxCapacityLessThanOnHandThrows() {
       
        assertThrows(IllegalStateException.class, () ->stock.updateMaxCapacity(5)
        );
    }

    @Tag("sanity")
    @DisplayName("toString should return  nonEmpty string")
    @Timeout(value = 50, unit = TimeUnit.MILLISECONDS)
    @Test
    void testToStringIsNotEmpty() {
        String s = stock.toString();
        assertNotNull(s);
        assertFalse(s.isBlank());
    }

   
    @Disabled("Future feature: performance / stress test")
    @Test
    void disabledFutureTest() {
      
   }
}
