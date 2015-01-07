package ws.prova.reference2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import ws.prova.kernel2.Goal;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.kernel2.cache.ProvaCacheState;
import ws.prova.kernel2.cache.Answers;

public class ProvaLiteralImpl implements Literal {

	private static final long serialVersionUID = 9180554897688659494L;

	protected final Predicate predicate;
	
	protected PList terms;

	protected boolean ground = false;

	protected String sourceCode;

	protected Map<String, List<Object>> metadata;

	protected int line;
	
	public static ThreadLocal<Map<Object, PObj>> tlVars = new ThreadLocal<Map<Object, PObj>>();

	public ProvaLiteralImpl(Predicate predicate, PList terms) {
		this.predicate = predicate;
		this.terms = terms;
	}

	public ProvaLiteralImpl(Predicate predicate, PList terms,
			Map<String, List<Object>> metadata) {
		this.predicate = predicate;
		this.terms = terms;
		this.metadata = metadata;
	}

	@Override
	public Predicate getPredicate() {
		return predicate;
	}

	@Override
	public PList getTerms() {
		return terms;
	}

	@Override
	public void setTerms(PList terms) {
		this.terms = terms;
	}
	
	@Override
	public int collectVariables(long ruleId, List<Variable> variables) {
		if( ground || terms==null )
			return -1;
		int rc = terms.collectVariables(ruleId, variables);
		if( rc<0 )
			ground = true;
		return rc;
	}

	@Override
	public PObj getRecursivelyAssigned() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Should not be called
	 */
	@Override
	public int computeSize() {
		return 0;
	}

//	/**
//	 * Offset does not apply here
//	 */
//	@Override
//	public void collectVariables(long ruleId, Vector<ProvaVariable> variables, int offset) {
//		terms.collectVariables(ruleId, variables);
//	}

	@Override
	public void substituteVariables(VariableIndex[] varsMap) {
		if( ground )
			return;
		if( terms!=null )
			terms.substituteVariables(varsMap);
	}

	@Override
	public boolean unify(PObj target, Unification unification) {
		Literal targetLiteral = (Literal) target;
		return terms.unify(targetLiteral.getTerms(), unification); 
	}

	@Override
	public Literal rebuild(final Unification unification) {
		if( ground || terms==null )
			return this;
		PList newTerms = terms.rebuild(unification);
		final ProvaLiteralImpl ret = new ProvaLiteralImpl(predicate, newTerms);
		ret.sourceCode = this.sourceCode;
		ret.line = this.line;
		if( this.metadata!=null )
			copyMetadata(unification, ret);
		return ret;
	}

	protected void copyMetadata(Unification unification,
		final ProvaLiteralImpl ret) {
		// Make variable substitutions in the metadata if it contains variables
		ret.metadata = new HashMap<String, List<Object>>(this.metadata);
		for( Entry<String,List<Object>> e : metadata.entrySet() ) {
			for( int i=0; i<e.getValue().size(); i++ ) {
				Object o = e.getValue().get(i);
				if( o instanceof Variable ) {
					Object oo = ((Variable) o).getAssigned();
					if( oo==null || !(oo instanceof Variable) )
						oo = o;
					for( int j=0; j<unification.getTarget().getVariables().size(); j++ ) {
						Variable var = unification.getTarget().getVariables().get(j);
						if( var==oo ) {
							Map<Object, PObj> vars = tlVars.get();
							if( vars==null ) {
								vars = new HashMap<Object, PObj>();
								tlVars.set(vars);
							}
							// Note that this entry must be cleared immediately after the metadata value is used.
							// So far only ProvaAndGroupImpl does that.
							vars.put(((Variable) o).getName(), ((Variable) unification.getTargetVariables().get(j)).getAssigned());
						}
					}
				}
			}
		}
	}

	@Override
	public Literal rebuildSource(Unification unification) {
		if( ground || terms==null )
			return this;
		PList newTerms = terms.rebuildSource(unification);
		return new ProvaLiteralImpl(predicate, newTerms, metadata);
	}

        @Override
	public String toString() {
		StringBuilder sb = new StringBuilder(predicate.getSymbol());
		sb.append('(');
		sb.append(terms);
		sb.append(')');
		return sb.toString();
	}

	@Override
	public void addClause(Rule clause) {
//		ProvaKnowledgeBase kb = predicate.getKnowledgeBase();
		predicate.addClause(clause);
//		kb.getPredicates(predicate.getSymbol(), predicate.getArity()).add(clause);
//		kb.getPredicates(predicate.getSymbol(), -1).add(clause);
	}

	@Override
	public void addClauseA(Rule clause) {
//		ProvaKnowledgeBase kb = predicate.getKnowledgeBase();
		predicate.addClauseA(clause);
//		kb.getPredicates(predicate.getSymbol(), predicate.getArity()).add(clause);
//		kb.getPredicates(predicate.getSymbol(), -1).add(clause);
	}

	@Override
	public boolean isGround() {
		return ground;
	}

	@Override
	public void setGoal(Goal goal) {
	}

	@Override
	public ProvaCacheState getCacheState() {
		// Should not be called
		return null;
	}

	@Override
	public Answers getAnswers() {
		// Should not be called
		return null;
	}

	@Override
	public void markCompletion() {
	}

	@Override
	public Goal getGoal() {
		// Should not be called
		return null;
	}

	@Override
	public String toString(List<Variable> variables) {
		return toString();
	}

	@Override
	public Literal cloneWithBoundVariables(final Unification unification,
			final List<Variable> variables, final List<Boolean> isConstant) {
		final ProvaLiteralImpl ret = (ProvaLiteralImpl) cloneWithBoundVariables(variables, isConstant);
		if( ret.getMetadata()!=null )
			copyMetadata(unification, ret);
		return ret;
	}

	@Override
	public PObj cloneWithBoundVariables(final List<Variable> variables, final List<Boolean> isConstant) {
		if( terms==null )
			return this;
		final PList newTerms = (PList) terms.cloneWithBoundVariables(variables, isConstant);
		final ProvaLiteralImpl newLit = new ProvaLiteralImpl(predicate,newTerms);
		// TODO: the new literal may actually become ground
		newLit.ground = ground;
		newLit.line = line;
		newLit.sourceCode = sourceCode;
		newLit.metadata = metadata;
		return newLit;
	}

	@Override
	public PObj cloneWithVariables(final List<Variable> variables) {
		if( terms==null )
			return this;
		if( predicate.getSymbol().equals("cut") ) {
			Variable any1 = ProvaVariableImpl.create();
			PList lany1 = ProvaListImpl.create(new PObj[] {any1});
			return new ProvaLiteralImpl(predicate,lany1);
		}
		final PList newTerms = (PList) terms.cloneWithVariables(variables);
		final ProvaLiteralImpl newLit = new ProvaLiteralImpl(predicate,newTerms);
		// TODO: the new literal may actually become ground
		newLit.ground = ground;
		newLit.line = line;
		newLit.sourceCode = sourceCode;
		newLit.metadata = metadata;
		return newLit;
	}

	@Override
	public PObj cloneWithVariables(final long ruleId, final List<Variable> variables) {
		if( terms==null )
			return this;
		if( predicate.getSymbol().equals("cut") ) {
			Variable any1 = ProvaVariableImpl.create();
			PList lany1 = ProvaListImpl.create(new PObj[] {any1});
			return new ProvaLiteralImpl(predicate,lany1);
		}
		final PList newTerms = (PList) terms.cloneWithVariables(ruleId,variables);
		final ProvaLiteralImpl newLit = new ProvaLiteralImpl(predicate,newTerms);
		newLit.ground = ground;
		newLit.line = line;
		newLit.sourceCode = sourceCode;
		newLit.metadata = metadata;
		return newLit;
	}

	@Override
	public String getSourceCode() {
		if( this.sourceCode==null)
			this.sourceCode = this.toString();
		return this.sourceCode;
	}

	@Override
	public void setSourceCode(String text) {
		this.sourceCode = text;
	}

	@Override
	public void setMetadata(String property, List<Object> value) {
		if( metadata==null )
			metadata = new HashMap<String,List<Object>>();
		metadata.put(property,value);
	}

	@Override
	public List<Object> getMetadata(String property) {
		return metadata==null ? null : metadata.get(property);
	}

	@Override
	public List<PObj> addMetadata( Map<String,List<Object>> m) {
		if( m==null )
			return null;
		if( metadata==null )
			metadata = new HashMap<String,List<Object>>();
		metadata.putAll(m);
		List<PObj> metaVariables = new ArrayList<PObj>();
		for( Entry<String, List<Object>> e : m.entrySet() ) {
			for( Object value : e.getValue() ) {
				if( !(value instanceof String) )
					continue;
				String str = (String) value;
				if( str.length()!=0 && Character.isUpperCase(str.charAt(0)) )
					metaVariables.add(ProvaListImpl.create(new PObj[] {ProvaConstantImpl.create(str), ProvaVariableImpl.create(str)}));
			}
		}
		return metaVariables.isEmpty() ? null : metaVariables;
	}

	@Override
	public Map<String, List<Object>> getMetadata() {
		return metadata;
	}

	@Override
	public void setLine(int line) {
		this.line = line;
	}
	
	@Override
	public int getLine() {
		return this.line;
	}

	@Override
	public List<Literal> getGuard() {
		return null;
	}

	@Override
	public Object computeIfExpression() {
		return this;
	}

	@Override
	public void setGround(boolean ground) {
		this.ground = ground;
		if( ground && terms!=null )
			terms.setGround(true);
	}

	@Override
	public boolean updateGround(List<Variable> variables) {
		return ground = terms.updateGround(variables);
	}

}
