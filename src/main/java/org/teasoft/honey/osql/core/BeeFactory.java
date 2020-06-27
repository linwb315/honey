package org.teasoft.honey.osql.core;

import javax.sql.DataSource;

import org.teasoft.bee.osql.BeeAbstractFactory;
import org.teasoft.honey.distribution.ds.Router;

/**
 * @author Kingstar
 * @since  1.0
 */
public class BeeFactory extends BeeAbstractFactory {

	private static BeeFactory instance=new BeeFactory();
	private static HoneyFactory honeyFactory = null;

	public void setHoneyFactory(HoneyFactory honeyFactory) {
		this.honeyFactory = honeyFactory;
	}

	public static HoneyFactory getHoneyFactory() {
		if (honeyFactory == null) {
			honeyFactory = new HoneyFactory();
		}
		return honeyFactory;
	}

	private BeeFactory() {
	}
	
	public static BeeFactory getInstance(){
		return instance;
	}
	
	@Override
	public DataSource getDataSource() {
		
		if(super.getDataSourceMap()==null){
			//System.err.println("-----------------------------this old road!");
		   return super.getDataSource();
		}else{
			//System.err.println("------------++++++++++++++------this new road!");
			return _getDsFromDsMap();
		}
	}
	
	private DataSource _getDsFromDsMap(){
		
		String dsName=Router.getDsName(); 
		return getDataSourceMap().get(dsName);
	}

}
