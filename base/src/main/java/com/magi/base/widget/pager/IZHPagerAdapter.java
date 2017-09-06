/*
 * Copyright (c) 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.magi.base.widget.pager;

import android.support.v4.app.Fragment;

import java.util.List;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-22-2017
 */

public interface IZHPagerAdapter {

    PagerItem getPagerItem(final int pPosition);

    CharSequence getPageTitle(final int position);

    int getCount();

    void addPagerItem(final PagerItem pagerItem);

    void addPagerItems(List<PagerItem> pagerItems);

    void setPagerItems(List<PagerItem> pagerItems, boolean forceClearOldItems);

    void clearItems();

    Fragment getCurrentPrimaryItem();

    Fragment retrieveFragment(int position);
}
