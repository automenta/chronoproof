package ws.prova.reference2;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import ws.prova.kernel2.Constant;
import ws.prova.kernel2.Derivation;
import ws.prova.kernel2.KB;
import ws.prova.kernel2.PList;
import ws.prova.kernel2.PListIndex;
import ws.prova.kernel2.Literal;
import ws.prova.kernel2.PObj;
import ws.prova.kernel2.Predicate;
import ws.prova.kernel2.Rule;
import ws.prova.kernel2.Unification;
import ws.prova.kernel2.Variable;
import ws.prova.kernel2.VariableIndex;
import ws.prova.reference2.builtins.ProvaFailImpl;

public class ProvaUnificationImpl implements Unification {

	private Rule source;
	
	private Rule target;

	private long sourceRuleId;
	
	private long targetRuleId;
	
	private List<Variable> sourceVariables;
	
	private List<Variable> targetVariables;

	private List<PList> meta;
	
	public ProvaUnificationImpl(Rule source, Rule target) {
		init(source,target,true);
	}
	
	public ProvaUnificationImpl(Rule source, Rule target, boolean cloneTarget) {
		init(source,target,cloneTarget);
	}
	
	private void init(Rule source, Rule target, boolean cloneTarget) {
		this.source = source;
		this.target = target;
		this.sourceRuleId = source.getRuleId();
		this.targetRuleId = target.getRuleId();
		this.sourceVariables = source.getVariables();
		if( cloneTarget )
			this.targetVariables = target.cloneVariables();
		else
			this.targetVariables = target.getVariables();
	}
	
	public void setSource(Rule source) {
		this.source = source;
	}

	@Override
	public Rule getSource() {
		return source;
	}

	public void setTarget(Rule target) {
		this.target = target;
	}

	@Override
	public Rule getTarget() {
		return target;
	}

	public void setSourceRuleId(long sourceRuleId) {
		this.sourceRuleId = sourceRuleId;
	}

	@Override
	public long getSourceRuleId() {
		return sourceRuleId;
	}

	public void setTargetRuleId(long targetRuleId) {
		this.targetRuleId = targetRuleId;
	}

	@Override
	public long getTargetRuleId() {
		return targetRuleId;
	}

	public void setSourceVariables(List<Variable> sourceVariables) {
		this.sourceVariables = sourceVariables;
	}

	@Override
	public List<Variable> getSourceVariables() {
		return sourceVariables;
	}

	public void setTargetVariables(List<Variable> targetVariables) {
		this.targetVariables = targetVariables;
	}

	@Override
	public List<Variable> getTargetVariables() {
		return targetVariables;
	}

	@Override
	public boolean unify() {
		Literal[] sourceLiterals = source.getBody();
		// TODO: throw something
//		if( sourceLiterals.length==0 )
//			throw 
		Literal sourceLiteral = sourceLiterals[source.getOffset()];
		if( !matchMetadata(sourceLiteral,target) )
			return false;
		Literal targetLiteral = target.getHead();
		Predicate sourcePredicate = sourceLiteral.getPredicate();
		Predicate targetPredicate = targetLiteral.getPredicate();

		return sourcePredicate.equals(targetPredicate) && sourceLiteral.unify(targetLiteral, this);
	}

	private boolean matchMetadata(final Literal sourceLiteral, final Rule target) {
		Map<String,List<Object>> sourceMetadata = sourceLiteral.getMetadata();
		if( sourceMetadata==null || sourceMetadata.isEmpty() )
			// No source metadata or only line number
			return true;
		Map<String, List<Object>> targetMetadata = target.getMetadata();
		if( targetMetadata==null )
			return false;
		// All requested metadata must be found in the target
		for( Entry<String, List<Object>> s : sourceMetadata.entrySet() ) {
			List<Object> value = targetMetadata.get(s.getKey());
			List<Object> sValue = s.getValue();
			if( value==null )
				return false;
			boolean matched = false;
			// Either of values in the source must be present in the list of values in the target
			for( Object vo : value ) {
				if( !(vo instanceof String) )
					continue;
				String v = (String) vo;
				for( Object sVo : sValue ) {
					if( !(sVo instanceof String) )
						continue;
					String sV = (String) sVo;
					if( sV.length()!=0 && Character.isUpperCase(sV.charAt(0)) ) {
						if( meta==null )
							// Should not normally happen
							return false;
						for( PList m : meta ) {
							PObj[] mo = m.getFixed();
							String varName = (String) ((Constant) mo[0]).getObject();
							PObj var = mo[1];
							if( varName.equals(sV) ) {
								if( mo[1] instanceof VariableIndex ) {
									VariableIndex varPtr = (VariableIndex) var;
									var = sourceVariables.get(varPtr.getIndex()).getRecursivelyAssigned();
								}
								if( var instanceof Variable ) {
									((Variable) var).setAssigned(ProvaConstantImpl.create(v));
									matched = true;
									break;
								} else if( var instanceof Constant ) {
									// This allows for dynamic instantiation of metadata values from bound variables
									sV = (String) ((Constant) var).getObject();
									break;
								}
							}
						}
					}
					if( matched )
						break;
					if( v.equals(sV) ) {
						matched = true;
						break;
					}
				}
				if( matched )
					break;
			}
			if( !matched )
				return false;
		}
		return true;
	}

	@Override
	public Literal[] rebuildNewGoals() {
		final Literal[] body = target.getBody();
		int bodyLength = body==null ? 0 : body.length;
		Literal[] goals = new ProvaLiteralImpl[bodyLength];
		for( int i=0; i<bodyLength; i++ ) {
			goals[i] = body[i].rebuild(this);
		}
		return goals;
	}

	private Literal[] rebuildNewGoals(final Derivation node) {
		if( target.getBody()==null || target.getBody().length==0 )
			return new Literal[0];
		boolean allGround = true;
		for( Variable var : targetVariables ) {
			if( !var.getRecursivelyAssigned().isGround() ) {
				allGround = false;
				break;
			}
		}
		final Literal[] body = target.getGuardedBody(source.getBody()[0]);
		final int bodyLength = body==null ? 0 : body.length;
		final Literal[] goals = new ProvaLiteralImpl[bodyLength];
		for( int i=0; i<bodyLength; i++ ) {
			if( "cut".equals(body[i].getPredicate().getSymbol()) ) {
				final VariableIndex any = (VariableIndex) body[i].getTerms().getFixed()[0];
				final ProvaConstantImpl cutnode = ProvaConstantImpl.create(node);
				if( any.getRuleId()==source.getRuleId() )
					sourceVariables.get(any.getIndex()).setAssigned(cutnode);
				else
					targetVariables.get(any.getIndex()).setAssigned(cutnode);
				goals[i] = new ProvaLiteralImpl(body[i].getPredicate(),ProvaListImpl.create(new PObj[] {cutnode}));
				continue;
			}
			goals[i] = body[i].rebuild(this);
			goals[i].setLine(body[i].getLine());
			if( allGround )
				goals[i].setGround(true);
		}
		return goals;
	}

	@Override
	public Literal[] rebuildOldGoals(final Literal[] body) {
		if( !isSourceSubstituted() )
			return body;
		final Literal[] goals = new ProvaLiteralImpl[body.length];
		// Index 0 contains the current goal that does not need to be rebuilt
		for( int i=1; i<body.length; i++ ) {
			goals[i] = body[i].rebuildSource(this);
		}
		return goals;
	}

	private Literal[] rebuildOldGoals(final Literal[] body, final int offset) {
		if( body[offset].isGround() )
			return body;

		final Literal[] goals = new ProvaLiteralImpl[body.length];
		// Index offset contains the current goal that does not need to be rebuilt
		for( int i=1+offset; i<body.length; i++ ) {
			goals[i] = body[i].rebuildSource(this);
		}
		return goals;
	}

	private boolean isSourceSubstituted() {
		for( Variable variable : sourceVariables )
			if( variable.getAssigned()!=null )
				return true;
		return false;
	}

	@Override
	public Variable getVariableFromVariablePtr(final VariableIndex variablePtr) {
		if( variablePtr.getRuleId()==sourceRuleId )
			return sourceVariables.get(variablePtr.getIndex());
		return targetVariables.get(variablePtr.getIndex());
	}

	@Override
	public PObj rebuild(final VariableIndex variablePtr) {
		final Variable variable = getVariableFromVariablePtr(variablePtr);
//		if( variable.getAssigned()==null && variablePtr.getRuleId()==sourceRuleId )
//			return variablePtr;
		final PObj assigned = variable.getRecursivelyAssigned();
		if( assigned.getClass()==ProvaConstantImpl.class )
			return assigned;
		if( assigned instanceof Variable ) {
			if( ((Variable) assigned).getRuleId()==targetRuleId ) {
				// This is a target variable so add it to the source variables
				int index = assigned.collectVariables(targetRuleId, sourceVariables);
				return new ProvaVariablePtrImpl(
						sourceRuleId,
						index);
			} else {
				return new ProvaVariablePtrImpl(
						sourceRuleId,
						((Variable) assigned).getIndex());
			}
		} else if( assigned instanceof PList ) {
			return ((PList) assigned).rebuild(this);
		} else if( assigned instanceof PListIndex ) {
			return ((PListIndex) assigned).rebuild(this);
		} else if( assigned instanceof ProvaMapImpl ) {
			return ((ProvaMapImpl) assigned).rebuild(this);
		}
		return assigned;
	}

	@Override
	public PObj rebuildSource(final VariableIndex variablePtr) {
		final Variable variable = getVariableFromVariablePtr(variablePtr);
		final PObj assigned = variable.getRecursivelyAssigned();
		
		if( assigned==variable && variablePtr.getRuleId()==sourceRuleId )
			return variablePtr;
		if( assigned.getClass()==ProvaConstantImpl.class )
			return assigned;
		if( assigned instanceof Variable ) {
			if( ((Variable) assigned).getRuleId()==targetRuleId ) {
				// This is a target variable so add it to the source variables
				int index = assigned.collectVariables(targetRuleId, sourceVariables);
				return new ProvaVariablePtrImpl(
						sourceRuleId,
						index);
			} else {
				return new ProvaVariablePtrImpl(
						sourceRuleId,
						((Variable) assigned).getIndex());
			}
		} else if( assigned instanceof PList ) {
			return ((PList) assigned).rebuildSource(this);
		} else if( assigned instanceof PListIndex ) {
			return ((PListIndex) assigned).rebuildSource(this);
		} else if( assigned instanceof ProvaMapImpl ) {
			return ((ProvaMapImpl) assigned).rebuildSource(this);
		}
		return assigned;
	}

	@Override
	public Rule generateQuery(String symbol, KB kb, Rule query, Derivation node) {
		if( sourceVariables.isEmpty() ) {
			return kb.newGoal(this, node, target.getBody(), query.getBody(), query.getOffset(), targetVariables);
		}
		final Literal[] newGoals = rebuildNewGoals(node);
		Literal[] oldGoals = null;
		Rule newQuery = null;
		if( newGoals.length!=0 && newGoals[newGoals.length-1].getPredicate() instanceof ProvaFailImpl ) {
			// fail() predicate in the target body cuts the goal trail
			newQuery = new Rule(0, null, newGoals);
		} else {
			oldGoals = rebuildOldGoals(query.getBody(), query.getOffset());
			newQuery = kb.newRule(null, newGoals, oldGoals, query.getOffset());
			if( oldGoals==query.getBody() ) {
				// Goal was ground
				if( newQuery.getVariables().isEmpty() ) {
					// Target body is fully ground
					newQuery.setVariables(sourceVariables);
					return newQuery;
				}
			}
		}
		return rebuild(newQuery);
	}

	private Rule rebuild(final Rule newQuery) {
		final int size = sourceVariables.size();
		if( size==0 )
			return newQuery;
		final VariableIndex[] varsMap = new VariableIndex[size];
		final List<Variable> newVariables = newQuery.getVariables();
		int index = 0;
		for( int i=0; i<size; i++ ) {
			if( sourceVariables.get(i).getAssigned()==null ) {
				varsMap[i] = new ProvaVariablePtrImpl(0,index);
				Variable newVariable = sourceVariables.get(i); //.clone();
				newVariable.setIndex(index++);
				newVariable.setRuleId(0);
				newVariables.add(newVariable);
			}
		}
		newQuery.substituteVariables(varsMap);

		return newQuery;
	}

	@Override
	public boolean targetUnchanged() {
		for( Variable var : targetVariables ) {
			if( var.getAssigned()!=null
				&&
				(!(var.getAssigned() instanceof Variable)
				||
				var.getType()!=((Variable) var.getAssigned()).getType()))
				return false;
		}
		return true;
	}

	public void setMeta(List<PList> meta) {
		if( meta!=null )
			this.meta = meta;
	}

}
