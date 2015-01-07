package ws.prova.reference2;

import java.util.ArrayList;
import java.util.List;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;

public class ProvaGuardedLiteralImpl extends ProvaLiteralImpl implements
		Literal {

	private static final long serialVersionUID = -3967549869751321929L;

	private List<Literal> guard;
	
	public ProvaGuardedLiteralImpl(Predicate pred, PList terms,
			List<Literal> guard) {
		super(pred,terms);
		this.guard = guard;
	}

	@Override
	public List<Literal> getGuard() {
		return guard;
	}
	
	@Override
	public int collectVariables(long ruleId, List<Variable> variables) {
		if( ground || terms==null )
			return -1;
		ground &= terms.collectVariables(ruleId, variables)<0;
		for( Literal g : guard ) {
			ground &= g.collectVariables(ruleId, variables)<0;
		}
		return ground ? -1 : 0;
	}

	@Override
	public void substituteVariables(VariableIndex[] varsMap) {
		super.substituteVariables(varsMap);
		for( Literal g : guard ) {
			g.substituteVariables(varsMap);
		}
	}

	@Override
	public Literal cloneWithBoundVariables(final Unification unification,
			final List<Variable> variables, final List<Boolean> isConstant) {
		ProvaGuardedLiteralImpl ret = (ProvaGuardedLiteralImpl) cloneWithBoundVariables(variables, isConstant);
		List<Literal> newGuard = new ArrayList<Literal>(guard.size());
		for( Literal g : guard )
			newGuard.add(g.cloneWithBoundVariables(unification, variables, isConstant));
		ret.guard = newGuard;
		if( ret.getMetadata()!=null )
			copyMetadata(unification, ret);
		return ret;
	}

	@Override
	public PObj cloneWithBoundVariables(final List<Variable> variables, final List<Boolean> isConstant) {
		if( terms==null )
			return this;
		PList newTerms = (PList) terms.cloneWithBoundVariables(variables, isConstant);
		ProvaGuardedLiteralImpl newLit = new ProvaGuardedLiteralImpl(predicate,newTerms,null);
		// TODO: the new literal may actually become ground
		newLit.ground = ground;
		newLit.line = line;
		newLit.sourceCode = sourceCode;
		newLit.metadata = metadata;
		return newLit;
	}

	@Override
	public Literal rebuild(final Unification unification) {
		if( ground || terms==null )
			return this;
		final PList newTerms = terms.rebuild(unification);
		final List<Literal> newGuard = new ArrayList<Literal>(guard.size());
		for( Literal g : guard )
			newGuard.add(g.rebuild(unification));
		final ProvaGuardedLiteralImpl ret = new ProvaGuardedLiteralImpl(predicate, newTerms, newGuard);
		ret.sourceCode = this.sourceCode;
		ret.line = this.line;
		if( this.metadata!=null )
			copyMetadata(unification, ret);
		return ret;
	}

	@Override
	public Literal rebuildSource(final Unification unification) {
		if( ground )
			return this;
		List<Literal> newGuard = new ArrayList<Literal>(guard.size());
		for( Literal g : guard )
			newGuard.add(g.rebuildSource(unification));
		PList newTerms = terms;
		if( terms!=null )
			newTerms = terms.rebuildSource(unification);
		return new ProvaGuardedLiteralImpl(predicate, newTerms, newGuard);
	}

	@Override
	public PObj cloneWithVariables(final List<Variable> variables) {
		if( predicate.getSymbol().equals("cut") ) {
			Variable any1 = ProvaVariableImpl.create();
			PList lany1 = ProvaListImpl.create(new PObj[] {any1});
			return new ProvaLiteralImpl(predicate,lany1);
		}
		PList newTerms = terms;
		if( terms!=null )
			newTerms = (PList) terms.cloneWithVariables(variables);
		List<Literal> newGuard = new ArrayList<Literal>(guard.size());
		for( Literal g : guard )
			newGuard.add((Literal) g.cloneWithVariables(variables));
		ProvaGuardedLiteralImpl newLit = new ProvaGuardedLiteralImpl(predicate,newTerms,newGuard);
		newLit.ground = ground;
		newLit.line = line;
		newLit.sourceCode = sourceCode;
		newLit.metadata = metadata;
		return newLit;
	}

	@Override
	public PObj cloneWithVariables(final long ruleId, final List<Variable> variables) {
		if( predicate.getSymbol().equals("cut") ) {
			Variable any1 = ProvaVariableImpl.create();
			PList lany1 = ProvaListImpl.create(new PObj[] {any1});
			return new ProvaLiteralImpl(predicate,lany1);
		}
		PList newTerms = terms;
		if( terms!=null )
			newTerms = (PList) terms.cloneWithVariables(ruleId,variables);
		final List<Literal> newGuard = new ArrayList<Literal>(guard.size());
		for( Literal g : guard )
			newGuard.add((Literal) g.cloneWithVariables(ruleId,variables));
		ProvaGuardedLiteralImpl newLit = new ProvaGuardedLiteralImpl(predicate,newTerms,newGuard);
		newLit.ground = ground;
		newLit.line = line;
		newLit.sourceCode = sourceCode;
		newLit.metadata = metadata;
		return newLit;
	}

        @Override
	public String toString() {
		StringBuilder sb = new StringBuilder(predicate.getSymbol());
		sb.append('(');
		sb.append(terms);
		sb.append(") [");
		sb.append(guard);
		sb.append(']');
		return sb.toString();
	}

}
