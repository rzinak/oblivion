package com.br;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.br.App.ObjectToJsonConverter;
import com.br.App.Person;
import org.junit.jupiter.api.Test;

/** Unit test for simple App. */
public class AppTest {

  /** Rigorous Test :-) */
  @Test
  public void shouldAnswerWithTrue() {
    assertTrue(true);
  }

  @Test
  public void givenObjectSerializedThenTrueReturned() throws Exception {
    Person person = new Person("renan", "araújo", "24", "RJ");
    ObjectToJsonConverter serializer = new ObjectToJsonConverter();
    String jsonString = serializer.convertToJson(person);
    assertEquals(
        "{\"personAge\":\"24\",\"firstName\":\"Renan\",\"lastName\":\"Araújo\"}", jsonString);
  }
}
