package station;

import driver.DriverPanel;
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

public class StationAgent extends Agent {

    private final Map<String, AID> agents = new HashMap<>();

    @Override
    protected void setup() {
        // Printout a welcome message
        System.out.println("Station agent " + getAID().getName() + " is ready.");

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

        addBehaviour(new ReceiveEnergyCommandBehaviour());
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
        System.out.println("Station agent " + getAID().getName() + " terminating.");
    }

    private class ReceiveEnergyCommandBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = receive(mt);
            if (msg != null) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.PROPOSE);
                send(reply);
                if (agents.isEmpty()) {
                    updateAgents(this.myAgent);
                }
                //doDelete();
                ACLMessage aclMessage = new ACLMessage(ACLMessage.INFORM);
                aclMessage.setContent("100");
                aclMessage.setConversationId("energy-trade");
                aclMessage.setReplyWith("cfp" + System.currentTimeMillis());
                aclMessage.addReceiver(agents.get(Utils.CAR_AGENT_NAME));
                send(aclMessage);
            } else {
                block();
            }
        }
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
