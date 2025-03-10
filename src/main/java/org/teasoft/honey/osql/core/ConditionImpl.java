/*
 * Copyright 2016-2019 the original author.All rights reserved.
 * Kingstar(honeysoft@126.com)
 * The license,see the LICENSE file.
 */

package org.teasoft.honey.osql.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.teasoft.bee.osql.Condition;
import org.teasoft.bee.osql.FunctionType;
import org.teasoft.bee.osql.IncludeType;
import org.teasoft.bee.osql.Op;
import org.teasoft.bee.osql.OrderType;
import org.teasoft.bee.osql.SuidType;
import org.teasoft.bee.osql.exception.BeeErrorGrammarException;
import org.teasoft.honey.osql.util.NameCheckUtil;

/**
 * @author Kingstar
 * @since  1.6
 */
public class ConditionImpl implements Condition {

	private SuidType suidType;
	private List<Expression> list = new ArrayList<>();
	private Set<String> whereField = new HashSet<>(); //条件表达式用到的字段
	private IncludeType includeType;
	
	private String selectField;
	private Boolean isForUpdate;
	
	private List<Expression> updateSetList = new ArrayList<>();
	private Set<String> updatefields = new HashSet<>();//update set 部分用到的字段
	
	private List<FunExpress> funExpList=new ArrayList<>();
	
	private boolean isStartGroupBy = true;
	private boolean isStartHaving = true;
	private boolean isStartOrderBy = true;

	private static String COMMA = ",";

	private Integer start;
	private Integer size;

	@Override
	public Condition start(Integer start) {
		this.start = start;
		return this;
	}

	@Override
	public Condition size(Integer size) {
		this.size = size;
		return this;
	}
	
	@Override
	public IncludeType getIncludeType() {
		return includeType;
	}

	@Override
	public Condition setIncludeType(IncludeType includeType) {
		this.includeType = includeType;
		return this;
	}

	@Override
	public Condition op(String field, Op Op, Object value) {
		
		checkField(field);
		list.add(new Expression(field, Op, value));
		this.whereField.add(field);
		return this;
	}
	
	@Override
	public Condition opWithField(String field1, Op Op, String field2) {
		checkField(field1);
		checkField(field2);
		
		Expression exp = new Expression(field1, Op, field2);
		exp.setOpNum(-3); // eg:field1=field2
		
		list.add(exp);
		this.whereField.add(field1);
//		this.fieldSet.add(field2);
		return this;
	}
	
	@Override
	public Set<String> getWhereFields() {
		return whereField;
	}

	@Override
	public Condition and() {
		Expression exp = new Expression();
		exp.setOpNum(1);
		exp.value = K.and;
		list.add(exp);

		return this;
	}

	@Override
	public Condition or() {
		Expression exp = new Expression();
		exp.setOpNum(1);
		exp.value = K.or;
		list.add(exp);

		return this;
	}

	@Override
	public Condition lParentheses() {
		Expression exp = new Expression();
		exp.setOpNum(-2);
		exp.value = "(";
		list.add(exp);

		return this;
	}

	@Override
	public Condition rParentheses() {
		Expression exp = new Expression();
		exp.setOpNum(-1);
		exp.value = ")";
		list.add(exp);

		return this;
	}

	@Override
	public Condition groupBy(String field) {
		checkField(field);
		Expression exp = new Expression();
		exp.fieldName = field;
		exp.opType = "groupBy";
		
		if (isStartGroupBy) {
			isStartGroupBy = false;
//			exp.value =" group by ";
			exp.value =" "+K.groupBy+" ";
		} else {
			//exp.fieldName=","+field; //不能这样写,field需要转换
			exp.value = COMMA;
		}
		list.add(exp);
		return this;
	}

//	@Override
//	public Condition having(String expressionStr) {
//		checkHavingException(expressionStr);
//		Expression exp = new Expression();
//		exp.opType = "having";
//		//exp.value
//		exp.opNum = 2;
//		exp.value2=expressionStr;
//		
//		if (isStartHaving) {
//			if(isStartGroupBy) throw new BeeErrorGrammarException("The 'having' must be after 'group by' !");
//			isStartHaving = false;
////			exp.value = " having ";
//			exp.value = " "+K.having+" ";
//		} else {
////			exp.value = " and ";
//			exp.value = " "+K.and+" ";
//		}
//				
//		list.add(exp);
//		return this;
//	}
	//closed. because can use:
//	 .having(FunctionType.COUNT, "*", Op.ge, 1)
//	 .having(FunctionType.COUNT, "distinct(userid)", Op.ge, 1)

	@Override
	public Condition having(FunctionType functionType, String field, Op Op, Number value) {
		checkField(field);
		Expression exp = new Expression();
		exp.opType = "having";
		//exp.value
		exp.fieldName=field;
		exp.value2=value;
		exp.value3=functionType.getName();
		exp.opNum = 5;
		exp.value4=Op.getOperator();
		
		if (isStartHaving) {
			if(isStartGroupBy) throw new BeeErrorGrammarException("The 'having' must be after 'group by' !");
			isStartHaving = false;
//			exp.value = " having ";
			exp.value = " "+K.having+" ";
		} else {
//			exp.value = " and ";
			exp.value = " "+K.and+" ";
		}
				
		list.add(exp);
		return this;
	}

	@Override
	public Condition orderBy(String field) {
		checkField(field);
		Expression exp = new Expression();
		exp.opType = "orderBy";
		//		exp.value
		exp.fieldName = field;
		exp.opNum = 2;

		if (isStartOrderBy) {
			isStartOrderBy = false;
//			exp.value = " order by ";
			exp.value = " "+K.orderBy+" ";
		} else {
			exp.value = COMMA;
		}
		list.add(exp);
		return this;
	}

	@Override
	public Condition orderBy(String field, OrderType orderType) {
		checkField(field);
		Expression exp = new Expression();
		exp.opType = "orderBy";
		//		exp.value
		exp.fieldName = field;
		exp.value2 = orderType.getName();
		exp.opNum = 3;

		if (isStartOrderBy) {
			isStartOrderBy = false;
//			exp.value = " order by ";
			exp.value = " "+K.orderBy+" ";
		} else {
			exp.value = COMMA;
		}
		list.add(exp);
		return this;
	}
	
	@Override
	public Condition orderBy(FunctionType functionType, String field, OrderType orderType) {
		checkField(field);
		Expression exp = new Expression();
		exp.opType = "orderBy";
		//		exp.value
		exp.fieldName = field;
		exp.value2 = orderType.getName();
		exp.value3=functionType.getName();
		exp.opNum = 4;

		if (isStartOrderBy) {
			isStartOrderBy = false;
//			exp.value = " order by ";
			exp.value = " "+K.orderBy+" ";
		} else {
			exp.value = COMMA;
		}
		list.add(exp);
		return this;
	}
	
	private void setForBetween(String field, Object low, Object high,String type){
		checkField(field);
		Expression exp = new Expression();
		exp.fieldName = field;
//		exp.opType = "between";
		exp.opType =type;
		exp.value=low;
		exp.value2=high;
		exp.opNum=3;  //即使不用也不能省,因为默认值是0会以为是其它的
		
		this.whereField.add(field);
		
		list.add(exp);
	}
	
	@Override
	public Condition between(String field, Number low, Number high) {
		
//		setForBetween(field, low, high, " between ");
		setForBetween(field, low, high, " "+K.between+" ");
		
		return this;
	}

	@Override
	public Condition notBetween(String field, Number low, Number high) {
		setForBetween(field, low, high, " "+K.notBetween+" ");
		
		return this;
	}

	@Override
	public Condition between(String field, String low, String high) {
		setForBetween(field, low, high, " "+K.between+" ");
		
		return this;
	}

	@Override
	public Condition notBetween(String field, String low, String high) {
		setForBetween(field, low, high, " "+K.notBetween+" ");
		
		return this;
	}
	
	@Override
	public void setSuidType(SuidType suidType) {
		this.suidType = suidType;
	}

	public SuidType getSuidType() {
		return suidType;
	}

	public List<Expression> getExpList() {
		//todo 若要自动调整顺序,可以在这改.  group by,having, order by另外定义,在这才添加到list.
		return list;
	}

	public Integer getStart() {
		return start;
	}

	public Integer getSize() {
		return size;
	}
	
	private static final String setAdd="setAdd";
	private static final String setMultiply="setMultiply";
	
	private static final String setAddField = "setAddField";
	private static final String setMultiplyField = "setMultiplyField";
	
	private static final String setWithField="setWithField"; //v1.9

	@Override
	public Condition setAdd(String field, Number num) {  //for field self
        return forUpdateSet(field, num, setAdd);
	}

	@Override
	public Condition setMultiply(String field, Number num) { //for field self
		 return forUpdateSet(field, num, setMultiply);
	}
	
	@Override
	public Condition setAdd(String field, String otherFieldName) {
		return forUpdateSet(field, otherFieldName, setAddField);
	}

	@Override
	public Condition setMultiply(String field, String otherFieldName) {
		return forUpdateSet(field, otherFieldName, setMultiplyField);
	}
	
	@Override
	public Condition setWithField(String field1, String field2) {
		return forUpdateSet(field1, field2, setWithField);
	}
	
	@Override
	public Condition set(String fieldNmae, Number num) {
		return _forUpdateSet2(fieldNmae, num);
	}

	@Override
	public Condition set(String fieldNmae, String value) {
		return _forUpdateSet2(fieldNmae, value);
	}
	
	@Override
	public Condition selectField(String fieldList) {
		checkField(fieldList);
		this.selectField=fieldList;
		
		return this;
	}
	
	@Override
	public Condition selectDistinctField(String fieldName) {
		funExpList.add(new FunExpress("distinct", fieldName, null));
		return this;
	}
	
	@Override
	public Condition selectDistinctField(String fieldName,String alias) {
		checkField(alias);
		funExpList.add(new FunExpress("distinct", fieldName, alias));
		return this;
	}

	@Override
	public String getSelectField(){
		return this.selectField;
	}

	public List<Expression> getUpdateExpList() {
		return updateSetList;
	}
	
	public List<FunExpress> getFunExpList() {
		return funExpList;
	}
	
	private Condition forUpdateSet(String field, String otherFieldName,String opType){
		checkField(otherFieldName);
		return _forUpdateSet(field, otherFieldName, opType);
	}
	
	private Condition forUpdateSet(String field, Number num,String opType){
		return _forUpdateSet(field, num, opType);
	}
	
	private Condition _forUpdateSet(String field, Object ojb,String opType){
		checkField(field);
		Expression exp = new Expression();
		exp.fieldName = field;
		exp.opType =opType; //"setAdd" or "setMultiply";  setAddField; setMultiplyField; setWithField
		exp.value=ojb;
		exp.opNum=1;  
		
		this.updatefields.add(field);
		updateSetList.add(exp);
		
		return this;
	}
	
	//set field=value
	private Condition _forUpdateSet2(String field, Object ojb) {
		checkField(field);
		Expression exp = new Expression();
		exp.fieldName = field;
	  //exp.opType =opType; 
		exp.value = ojb;
		exp.opNum = 1;

		this.updatefields.add(field);
		updateSetList.add(exp);

		return this;
	}
	
	@Override
	public Set<String> getUpdatefields() {
		return updatefields;
	}

	@Override
	public Condition forUpdate() {
		isForUpdate = true;
		return this;
	}

	@Override
	public Boolean getForUpdate() {
		return isForUpdate;
	}
	
	//v1.9
	@Override
	public Condition selectFun(FunctionType functionType, String fieldForFun) {
		funExpList.add(new FunExpress(functionType, fieldForFun, null));
		return this;
	}

	//v1.9
	@Override
	public Condition selectFun(FunctionType functionType, String fieldForFun, String alias) {
		checkField(alias);
		funExpList.add(new FunExpress(functionType, fieldForFun, alias));
		return this;
	}

	private void checkField(String field){
//		if(CheckField.isIllegal(field)) {
//			throw new BeeErrorFieldException("The field: '"+field+ "' is illegal!");
//		}
		NameCheckUtil.checkName(field);
	}

	final class FunExpress{
//		private FunctionType functionType;
		private String functionType;
		private String field;
		private String alias;
		
		public FunExpress(FunctionType functionType, String field, String alias) {
			checkField(field);
			this.functionType = functionType.getName();
			this.field = field;
			this.alias = alias;
		}
		
		public FunExpress(String functionType, String field, String alias) {
			checkField(field);
			this.functionType = functionType;
			this.field = field;
			this.alias = alias;
		}

		FunExpress() {}

		public String getFunctionType() {
			return functionType;
		}

		public void setFunctionType(String functionType) {
			this.functionType = functionType;
		}

		public String getField() {
			return field;
		}
		
		public void setField(String field) {
			this.field = field;
		}

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}
	}
	
}
