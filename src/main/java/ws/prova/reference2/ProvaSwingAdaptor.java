package ws.prova.reference2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import ws.prova.agent2.Reagent;
import ws.prova.agent2.ProvaThreadpoolEnum;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.messaging.ProvaMessenger;

/**
 * Adaptor for Swing events. This class is created and used by ws.prova.agent2.ProvaReagentImpl.
 */
public class ProvaSwingAdaptor implements
		ActionListener,
		ChangeListener,
		MouseListener,
		MouseMotionListener {

	private final Reagent prova;

	private final KB kb;
	
	@SuppressWarnings("unused")
	private final ProvaMessenger messenger;

	public ProvaSwingAdaptor(Reagent prova) {
		this.prova = prova;
		this.kb = prova.getKb();
		this.messenger = prova.getMessenger();
	}

	public void listen(String type, Object target) {
		prova.setAllowedShutdown(false);
            switch (type) {
                case "action":
                    ((javax.swing.AbstractButton) target).addActionListener(this);
                    break;
                case "change":
                    ((javax.swing.AbstractButton) target).addChangeListener(this);
                    break;
                case "mouse":
                    ((java.awt.Component) target).addMouseListener(this);
                    break;
                case "motion":
                    ((java.awt.Component) target).addMouseMotionListener(this);
                    break;
            }
	}
	
	public void unlisten(String type, Object target) {
            switch (type) {
                case "action":
                    ((javax.swing.AbstractButton) target).removeActionListener(this);
                    break;
                case "change":
                    ((javax.swing.AbstractButton) target).removeChangeListener(this);
                    break;
                case "mouse":
                    ((java.awt.Component) target).removeMouseListener(this);
                    break;
                case "motion":
                    ((java.awt.Component) target).removeMouseMotionListener(this);
                    break;
            }
	}
	
	/**
	 * actionPerformed
	 *
	 * @param e ActionEvent
	 */
        @Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		Object cmd = e.getActionCommand();
		PList terms = ProvaListImpl.create(new PObj[] {
				ProvaConstantImpl.create("s"),
				ProvaConstantImpl.create("task"),
				ProvaConstantImpl.create("0"),
				ProvaConstantImpl.create("swing"),
				ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create("action"),
						ProvaConstantImpl.create(cmd),
						ProvaConstantImpl.create(src),
						ProvaConstantImpl.create(e)})});
		Literal lit = kb.newHeadLiteral("rcvMsg",terms);
		Rule goal = kb.newGoal(new Literal[] {lit,kb.newLiteral("fail")});
		prova.submitAsync(0,goal,ProvaThreadpoolEnum.TASK);
	}

	/**
	 * stateChanged
	 *
	 * @param e ChangeEvent
	 */
        @Override
	public void stateChanged(ChangeEvent e) {
		Object src = e.getSource();
		PList terms = ProvaListImpl.create(new PObj[] {
				ProvaConstantImpl.create("s"),
				ProvaConstantImpl.create("task"),
				ProvaConstantImpl.create("0"),
				ProvaConstantImpl.create("swing"),
				ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create("change"),
						ProvaConstantImpl.create(src),
						ProvaConstantImpl.create(e)})});
		Literal lit = kb.newHeadLiteral("rcvMsg",terms);
		Rule goal = kb.newGoal(new Literal[] {lit,kb.newLiteral("fail")});
		prova.submitAsync(0,goal,ProvaThreadpoolEnum.TASK);
	}

	/**
	 * mouseClicked
	 *
	 * @param e MouseEvent
	 */
        @Override
	public void mouseClicked(MouseEvent e) {
		Object src = e.getSource();
		PList terms = ProvaListImpl.create(new PObj[] {
				ProvaConstantImpl.create("s"),
				ProvaConstantImpl.create("task"),
				ProvaConstantImpl.create("0"),
				ProvaConstantImpl.create("swing"),
				ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create("mouse"),
						ProvaConstantImpl.create("clicked"),
						ProvaConstantImpl.create(src),
						ProvaConstantImpl.create(e)})});
		Literal lit = kb.newHeadLiteral("rcvMsg",terms);
		Rule goal = kb.newGoal(new Literal[] {lit,kb.newLiteral("fail")});
		prova.submitAsync(0,goal,ProvaThreadpoolEnum.TASK);
	}

	/**
	 * mouseEntered
	 *
	 * @param e MouseEvent
	 */
        @Override
	public void mouseEntered(MouseEvent e) {
		Object src = e.getSource();
		PList terms = ProvaListImpl.create(new PObj[] {
				ProvaConstantImpl.create("s"),
				ProvaConstantImpl.create("task"),
				ProvaConstantImpl.create("0"),
				ProvaConstantImpl.create("swing"),
				ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create("mouse"),
						ProvaConstantImpl.create("entered"),
						ProvaConstantImpl.create(src),
						ProvaConstantImpl.create(e)})});
		Literal lit = kb.newHeadLiteral("rcvMsg",terms);
		Rule goal = kb.newGoal(new Literal[] {lit,kb.newLiteral("fail")});
		prova.submitAsync(0,goal,ProvaThreadpoolEnum.TASK);
	}

	/**
	 * mouseExited
	 *
	 * @param e MouseEvent
	 */
        @Override
	public void mouseExited(MouseEvent e) {
		Object src = e.getSource();
		PList terms = ProvaListImpl.create(new PObj[] {
				ProvaConstantImpl.create("s"),
				ProvaConstantImpl.create("task"),
				ProvaConstantImpl.create("0"),
				ProvaConstantImpl.create("swing"),
				ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create("mouse"),
						ProvaConstantImpl.create("exited"),
						ProvaConstantImpl.create(src),
						ProvaConstantImpl.create(e)})});
		Literal lit = kb.newHeadLiteral("rcvMsg",terms);
		Rule goal = kb.newGoal(new Literal[] {lit,kb.newLiteral("fail")});
		prova.submitAsync(0,goal,ProvaThreadpoolEnum.TASK);
	}

	/**
	 * mousePressed
	 *
	 * @param e MouseEvent
	 */
        @Override
	public void mousePressed(MouseEvent e) {
		Object src = e.getSource();
		PList terms = ProvaListImpl.create(new PObj[] {
				ProvaConstantImpl.create("s"),
				ProvaConstantImpl.create("task"),
				ProvaConstantImpl.create("0"),
				ProvaConstantImpl.create("swing"),
				ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create("mouse"),
						ProvaConstantImpl.create("pressed"),
						ProvaConstantImpl.create(src),
						ProvaConstantImpl.create(e)})});
		Literal lit = kb.newHeadLiteral("rcvMsg",terms);
		Rule goal = kb.newGoal(new Literal[] {lit,kb.newLiteral("fail")});
		prova.submitAsync(0,goal,ProvaThreadpoolEnum.TASK);
	}

	/**
	 * mouseReleased
	 *
	 * @param e MouseEvent
	 */
        @Override
	public void mouseReleased(MouseEvent e) {
		Object src = e.getSource();
		PList terms = ProvaListImpl.create(new PObj[] {
				ProvaConstantImpl.create("s"),
				ProvaConstantImpl.create("task"),
				ProvaConstantImpl.create("0"),
				ProvaConstantImpl.create("swing"),
				ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create("mouse"),
						ProvaConstantImpl.create("released"),
						ProvaConstantImpl.create(src),
						ProvaConstantImpl.create(e)})});
		Literal lit = kb.newHeadLiteral("rcvMsg",terms);
		Rule goal = kb.newGoal(new Literal[] {lit,kb.newLiteral("fail")});
		prova.submitAsync(0,goal,ProvaThreadpoolEnum.TASK);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Object src = e.getSource();
		PList terms = ProvaListImpl.create(new PObj[] {
				ProvaConstantImpl.create("s"),
				ProvaConstantImpl.create("task"),
				ProvaConstantImpl.create("0"),
				ProvaConstantImpl.create("swing"),
				ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create("motion"),
						ProvaConstantImpl.create("dragged"),
						ProvaConstantImpl.create(src),
						ProvaConstantImpl.create(e)})});
		Literal lit = kb.newHeadLiteral("rcvMsg",terms);
		Rule goal = kb.newGoal(new Literal[] {lit,kb.newLiteral("fail")});
		prova.submitAsync(0,goal,ProvaThreadpoolEnum.TASK);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Object src = e.getSource();
		PList terms = ProvaListImpl.create(new PObj[] {
				ProvaConstantImpl.create("s"),
				ProvaConstantImpl.create("task"),
				ProvaConstantImpl.create("0"),
				ProvaConstantImpl.create("swing"),
				ProvaListImpl.create(new PObj[] {
						ProvaConstantImpl.create("motion"),
						ProvaConstantImpl.create("moved"),
						ProvaConstantImpl.create(src),
						ProvaConstantImpl.create(e)})});
		Literal lit = kb.newHeadLiteral("rcvMsg",terms);
		Rule goal = kb.newGoal(new Literal[] {lit,kb.newLiteral("fail")});
		prova.submitAsync(0,goal,ProvaThreadpoolEnum.TASK);
	}
}
