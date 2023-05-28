import jason.asSyntax.*;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.logging.Logger;

public class AgentEnv extends Environment {

    public static final int GSize = 7; // rezga izmers
    public static final Term next_cell = Literal.parseLiteral("next(cell)");

    private Image droneImage;

    static Logger logger = Logger.getLogger(AgentEnv.class.getName());
    public static final int CELL_SIZE = 80;

    private AgentEnvModel model;
    private AgentEnvGui view;

    @Override
    public void init(String[] args) {
        model = new AgentEnvModel();
        view = new AgentEnvGui(model);
        model.setView(view);
        updatePercepts();
    }

    public AgentEnv() {
        try {

            droneImage = ImageIO.read(new File("drone.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        //int agentId = Integer.parseInt(ag.substring(ag.length() - 1));  // Agentu klases nosaukumi beidzas ar ID numuru

        int agentId = Integer.parseInt(ag.substring(ag.length() - 1)) - 1;//ši rinda ir izmantota tikai ar vienu aģentu

        logger.info(ag + " is: " + action);
        try {
            if (action.equals(next_cell)) {
                model.nextCell();
            } else if (action.getFunctor().equals("move_to_the_next_cell")) {
                int x = (int) ((NumberTerm) action.getTerm(0)).solve();
                int y = (int) ((NumberTerm) action.getTerm(1)).solve();
                model.moveToNextCell(x, y);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updatePercepts();

        try {
            Thread.sleep(200);
        } catch (Exception e) {
        }

        if (model.getBatteryLoad(agentId) <= 0) {
            System.out.println("Agent " + ag + " battery level is 0%. Stopping the programm.");
            System.exit(0); // Iziet no programmas, kad akumulatora limenis ir 0%
        }

        informAgsEnvironmentChanged();

        return true;
    }

    void updatePercepts() {
        clearPercepts();

        for (int i = 0; i < 1; i++) {//agentu skaits, to var mainit
            Location agentLoc = model.getAgPos(i);

            Literal pos = Literal.parseLiteral("agent(Drone," + agentLoc.x + "," + agentLoc.y + ",id(" + i + "))");

            addPercept(pos);
        }
    }

    class AgentEnvModel extends GridWorldModel {

        public int[] batteryLoad = new int[10];//Agentu uzlades limenis ir 100%

        private AgentEnvModel() {

            super(GSize, GSize, 1); // Agentu skaits

            // agentu atrasanas vietas
            try {
                setAgPos(0, 0, 0);
               /* setAgPos(1, GSize-1, GSize-1);// 2 agents
                setAgPos(2, GSize-2, GSize-1);// 3 agents
                setAgPos(3, GSize-3, GSize-1);// 4 agents
                setAgPos(4, GSize-4, GSize-1);// 5 agents
                setAgPos(5, GSize-2, GSize-1);// 6 agents
                setAgPos(6, GSize-1, GSize-2);// 7 agents
                setAgPos(7, GSize-4, GSize-1);// 8 agents
                setAgPos(8, GSize-4, GSize-3);// 9 agents
                setAgPos(9, GSize-5, GSize-1);// 10 agents*/

                for (int i = 0; i < 1; i++) {//agentu skaits, to var mainit
                    batteryLoad[i] = 100;
                }

                //Location post1Loc = new Location(GSize - 1, GSize - 1);
                //setAgPos(1, post1Loc);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public int getBatteryLoad(int agentId) {
            return batteryLoad[agentId];
        }


        void nextCell() throws Exception {
            for (int i = 0; i < 1; i++) {//agentu skaits, to var mainit
                Location agent = getAgPos(i);
                agent.x++;
                if (agent.x == getWidth()) {
                    agent.x = 0;
                    agent.y++;
                }
                if (agent.y == getHeight()) {
                    agent.y = 0;
                }
                setAgPos(i, agent);
                batteryLoad[i] -= 0.5;  // Uzlades limena samazinasanas procents
                if (batteryLoad[i] < 0) {
                    batteryLoad[i] = 0;
                }
            }
            view.repaint();
        }

        void moveToNextCell(int x, int y) throws Exception {
            for (int i = 0; i < 1; i++) {//agentu skaits, to var mainit
                Location agent = getAgPos(i);
                if (agent.x < x)
                    agent.x++;
                else if (agent.x > x)
                    agent.x--;
                if (agent.y < y)
                    agent.y++;
                else if (agent.y > y)
                    agent.y--;
                setAgPos(i, agent);
            }
        }


    }

    class AgentEnvGui extends GridWorldView {

        public AgentEnvGui(AgentEnvModel model) {
            super(model, "Drone Agent Environment", 600);

            Timer timer = new Timer(50, new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    repaint();
                }
            });
            timer.start();

            setVisible(true);
            repaint();
        }

        @Override
        public void draw(Graphics g, int x, int y, int object) {
            switch (object) {
            }
        }

        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
            if (((AgentEnvModel) model).batteryLoad[id] > 0) {
                int agentSize = CELL_SIZE * 3 / 4;
                int agentOffset = (CELL_SIZE - agentSize) / 3;
                g.drawImage(droneImage, x * CELL_SIZE + agentOffset, y * CELL_SIZE + agentOffset, agentSize, agentSize, null);
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                String batteryLoadStr = ((AgentEnvModel) model).batteryLoad[id] + "%";
                int strWidth = g.getFontMetrics().stringWidth(batteryLoadStr);
                int strX = x * CELL_SIZE + (CELL_SIZE - strWidth) / 2;
                int strY = y * CELL_SIZE + CELL_SIZE / 5;
                g.drawString(batteryLoadStr, strX, strY);
            }
        }
    }
}