import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

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

    //step 5
    @Test
    void listFindsCatFactByItsValues() {
        List<App.CatFact> facts = new ArrayList<>();
        facts.add(new App.CatFact(1, "Cats have three eyelids"));

        boolean found = facts.contains(new App.CatFact(1, "Cats have three eyelids"));

        assertTrue(found);
    }

    //step 6
    @Test
    void newFactGivesBackTheTextItWasBuiltWith() {
        String text = "Cats have three eyelids";

        App.NewFact newFact = new App.NewFact(text);

        assertEquals(text, newFact.fact());
    }

    //step 7
    @Test
    void errorBodyGivesBackTheMessageItWasBuiltWith() {
        String message = "no fact with id 99";

        App.ErrorBody errorBody = new App.ErrorBody(message);

        assertEquals(message, errorBody.error());
    }
}