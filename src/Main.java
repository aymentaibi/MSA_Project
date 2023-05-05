import jade.core.Agent;

import static java.lang.Thread.sleep;

public class Main extends Agent {
    public static void main(String[] args) throws InterruptedException {
        String[] jadeArg = new String[2];
        StringBuffer sbAgent = new StringBuffer();
        sbAgent.append("A1:Acheteur('mohamed', 1500);");
        sbAgent.append("A2:Acheteur('amine', 2000);");
        sbAgent.append("A3:Acheteur('djafer', 5000);");
        sleep(5000);
        sbAgent.append("V1:Vendeur(1000,2000);");
        jadeArg[0] = "-gui";
        jadeArg[1] = sbAgent.toString();
        jade.Boot.main(jadeArg);
    }
}