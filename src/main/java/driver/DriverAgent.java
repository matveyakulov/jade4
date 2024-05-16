package driver;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class DriverAgent extends Agent {

    private final DriverPanel driverPanel = new DriverPanel();
    private final Map<String, AID> agents = new HashMap<>();

    @Override
    protected void setup() {
        // Printout a welcome message
        System.out.println("Driver agent " + getAID().getName() + " is ready.");

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("transport");
        sd.setName(Utils.DRIVER_AGENT_NAME);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour() {

            @Override
            public void action() {
                if (driverPanel.isNeedSendCommand()) {
                    sendCommand(driverPanel.getDirection(), driverPanel.isRunning(), driverPanel.isNeedCharge());
                    driverPanel.setNeedSendCommand(false);
                    driverPanel.setNeedCharge(false);
                }
            }
        });

        addBehaviour(new ReceiveBlockedCommandBehaviour());
        addBehaviour(new ReceiveEnergyBehaviour());
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
        System.out.println("Driver agent " + getAID().getName() + " terminating.");
    }

    public void sendCommand(DriverPanel.Direction direction, boolean running,  boolean needCharge) {
        if (agents.isEmpty()) {
            try {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("transport");
                template.addServices(sd);
                DFAgentDescription[] result = DFService.search(this, template);
                for (DFAgentDescription dfAgentDescription : result) {
                    AID aid = dfAgentDescription.getName();
                    agents.put(aid.getLocalName(), aid);
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
        ACLMessage aclMessage = new ACLMessage(ACLMessage.INFORM);
        aclMessage.setContent(String.format("%s %s %s", direction.name(), running, needCharge));
        aclMessage.setConversationId("transport-trade");
        aclMessage.setReplyWith("cfp" + System.currentTimeMillis());
        aclMessage.addReceiver(agents.get(Utils.CAR_AGENT_NAME));
        send(aclMessage);
    }

    private class ReceiveBlockedCommandBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            try {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.FAILURE);
                ACLMessage msg = receive(mt);
                if (msg != null) {
                    DriverPanel.Direction direction = DriverPanel.Direction.valueOf(
                            msg.getContent()
                    );
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.PROPOSE);
                    send(reply);
                    System.out.println("Receive blocked command: " + direction.name());
                } else {
                    block();
                }
            } catch (Exception e) {
                System.out.println("Receive blocked command: " + e.getMessage());
            }
        }
    }

    private class ReceiveEnergyBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = receive(mt);
            if (msg != null) {
                int energy = Integer.parseInt(msg.getContent());
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                send(reply);
                if (energy <= 10) {
                    ACLMessage aclMessage = new ACLMessage(ACLMessage.INFORM);
                    aclMessage.setConversationId("transport-trade");
                    aclMessage.setReplyWith("cfp" + System.currentTimeMillis());
                    aclMessage.addReceiver(agents.get(Utils.STATION_AGENT_NAME));
                    send(aclMessage);
                }
            } else {
                block();
            }
        }
    }
}
