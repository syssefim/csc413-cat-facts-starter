import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void catFactKeepsTheValuesItWasGiven() {
        int id = 7;
        String text = "Cats have three eyelids";

        App.CatFact fact = new App.CatFact(id, text);

        assertEquals(id, fact.id());
        assertEquals(text, fact.fact());
    }
}