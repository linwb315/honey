/*
 * Copyright 2016-2020 the original author.All rights reserved.
 * Kingstar(honeysoft@126.com)
 * The license,see the LICENSE file.
 */

package org.teasoft.honey.osql.dialect;

import org.teasoft.bee.osql.dialect.DbFeature;
import org.teasoft.honey.osql.core.HoneyUtil;

/**
 * @author Kingstar
 * @since  1.8.6
 */
public class LimitOffsetPaging implements DbFeature {

	@Override
	public String toPageSql(String sql, int offset, int size) {
		if (HoneyUtil.isRegPagePlaceholder()) {
			int array[] = new int[2];
			array[0] = size;
			array[1] = offset;
			HoneyUtil.regPageNumArray(array);
			return sql + " limit ? offset ?";
		} else {
			return sql + " limit " + size + " offset " + offset;
		}
	}

	@Override
	public String toPageSql(String sql, int size) {
		if (HoneyUtil.isRegPagePlaceholder()) {
			int array[] = new int[1];
			array[0] = size;
			HoneyUtil.regPageNumArray(array);
			return sql + " limit ?";
		} else {
			return sql + " limit " + size;
		}
	}

}
