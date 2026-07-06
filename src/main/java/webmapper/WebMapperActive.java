package webmapper;

import de.learnlib.driver.simulator.MealySimulatorSUL;
import de.learnlib.sul.SUL;

public class WebMapperActive {
    private final SUL<String, String> webSul;

    public WebMapperActive(){
        this.webSul = new MealySimulatorSUL<>(null);
    }


}
