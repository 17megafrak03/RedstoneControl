package cd4017be.rscpl.gui;

import cd4017be.rscpl.editor.Gate;
import cd4017be.rscpl.editor.TraceNode;
import cd4017be.rscpl.graph.Operator;

/**
 * 
 * @author CD4017BE
 *
 */
public class PinRef {

	public final int gate, pin, trace;
	public final int x, y;
	public final PinRef link;

	public PinRef(Operator out) {
		Gate<?> g = out.getGate();
		this.gate = g.index;
		this.pin = out.getPin();
		this.trace = -1;
		this.x = g.rasterX + g.type.width;
		this.y = g.rasterY + g.getOutputHeight(pin);
		this.link = null;
	}

	public PinRef(Gate<?> gate, int pin) {
		this.gate = gate.index;
		this.pin = pin;
		this.trace = 0;
		this.x = gate.rasterX;
		this.y = gate.rasterY + gate.getInputHeight(pin);
		TraceNode tn = gate.traces[pin];
		if (tn == null) {
			Operator out = gate.getInput(pin);
			this.link = out != null ? new PinRef(out) : null;
		} else this.link = new PinRef(tn, 1);
	}

	public PinRef(TraceNode tn, int depth) {
		this.gate = tn.owner.index;
		this.pin = tn.pin;
		this.trace = depth;
		this.x = tn.rasterX;
		this.y = tn.rasterY;
		if (tn.next == null) {
			Operator out = tn.owner.getInput(pin);
			this.link = out != null ? new PinRef(out) : null;
		} else this.link = new PinRef(tn.next, depth + 1);
	}

	public PinRef(PinRef ref, int x, int y) {
		this.gate = ref.gate;
		this.pin = ref.pin;
		this.trace = ref.trace + 1;
		this.x = x;
		this.y = y;
		this.link = ref.link;
	}

	@Override
	public int hashCode() {
		return x & 0xffff | y << 16;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PinRef)) return false;
		PinRef p = (PinRef)o;
		return gate == p.gate && pin == p.pin && trace == p.trace;
	}

}
