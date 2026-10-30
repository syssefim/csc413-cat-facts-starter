import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    //step 2
    @Test
    void catFactKeepsTheValuesItWasGiven() {
        int id = 7;
        String text = "Cats have three eyelids";

        App.CatFact fact = new App.CatFact(id, text);

        assertEquals(id, fact.id());
        assertEquals(text, fact.fact());
    }

    //step 3
    @Test
    void twoCatFactsWithTheSameValuesAreEqual() {
        int id = 7;
        String text = "Cats have three eyelids";

        App.CatFact first = new App.CatFact(id, text);
        App.CatFact second = new App.CatFact(id, text);

        assertEquals(first, second);
    }

    //step 4
    @Test
    void twoCatFactsWithDifferentTextAreNotEqual() {
        int id = 7;

        App.CatFact first = new App.CatFact(id, "Cats have three eyelids");
        App.CatFact second = new App.CatFact(id, "A group of cats is called a clowder");

        assertNotEquals(first, second);
    }
}