package car;

import driver.DriverPanel;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class CarAgent extends Agent {

    private final CarFrame carFrame = new CarFrame();
    private final CarPanel carPanel = carFrame.getCarPanel();
    private final Map<String, AID> agents = new HashMap<>();
    private int energy = 100;

    @Override
    protected void setup() {
        // Printout a welcome message
        System.out.println("Car agent " + getAID().getName() + " is ready.");

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("transport");
        sd.setName(Utils.CAR_AGENT_NAME);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new ReceiveCommandBehaviour());
        addBehaviour(new BlockedMoveCommandBehaviour());
        addBehaviour(new EnergyBehaviour(this, CarPanel.delay));
    }

    @Override
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("Car agent " + getAID().getName() + " terminating.");
    }

    private class ReceiveCommandBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = receive(mt);
            if (msg != null) {
                if (msg.getContent().contains(" ")) {
                    DriverPanel.Direction direction = DriverPanel.Direction.valueOf(
                            msg.getContent().split("\\s")[0]
                    );
                    boolean running = Boolean.parseBoolean(msg.getContent().split("\\s")[1]);
                    boolean needCharge = Boolean.parseBoolean(msg.getContent().split("\\s")[2]);
                    carPanel.setRunning(running);
                    carPanel.setDirection(direction);
                    if (needCharge) {
                        ACLMessage aclMessage = new ACLMessage(ACLMessage.INFORM);
                        aclMessage.setConversationId("transport-trade");
                        aclMessage.setReplyWith("cfp" + System.currentTimeMillis());
                        aclMessage.addReceiver(agents.get(Utils.STATION_AGENT_NAME));
                        send(aclMessage);
                    }
                } else {
                    int newEnergy = Integer.parseInt(msg.getContent());
                    carFrame.updateEnergy(newEnergy);
                    energy = newEnergy;
                }
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                send(reply);
            } else {
                block();
            }
        }
    }

    private class BlockedMoveCommandBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            if (!carPanel.isRunning()) {
                if (agents.isEmpty()) {
                    updateAgents(this.myAgent);
                }
                ACLMessage aclMessage = new ACLMessage(ACLMessage.FAILURE);
                aclMessage.setContent(carPanel.getDirection().name());
                aclMessage.setConversationId("transport-trade");
                aclMessage.setReplyWith("cfp" + System.currentTimeMillis());
                aclMessage.addReceiver(agents.get(Utils.DRIVER_AGENT_NAME));
                send(aclMessage);
            } else {
                block();
            }
        }
    }

    private class EnergyBehaviour extends TickerBehaviour {

        public EnergyBehaviour(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            if (carPanel.isRunning()) {
                if (agents.isEmpty()) {
                    updateAgents(this.myAgent);
                }
                energy -= getNormalEnergy();
                carFrame.updateEnergy(energy);
                ACLMessage aclMessage = new ACLMessage(ACLMessage.INFORM);
                aclMessage.setContent("" + energy);
                aclMessage.setConversationId("transport-trade");
                aclMessage.setReplyWith("cfp" + System.currentTimeMillis());
                aclMessage.addReceiver(agents.get(Utils.DRIVER_AGENT_NAME));
                send(aclMessage);
            } else {
                block();
            }
        }
    }

    private int getNormalEnergy() {
        return (int) Math.round(Math.log(Math.random()) / -1.5);
    }

    private void updateAgents(Agent agent) {
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("transport");
            template.addServices(sd);
            DFAgentDescription[] result = DFService.search(agent, template);
            for (DFAgentDescription dfAgentDescription : result) {
                AID aid = dfAgentDescription.getName();
                agents.put(aid.getLocalName(), aid);
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}
