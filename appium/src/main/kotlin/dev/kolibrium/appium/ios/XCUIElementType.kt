/*
 * Copyright 2023-2026 Attila Fazekas & contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.kolibrium.appium.ios

/**
 * Constants for XCUITest element type names used in iOS locator strategies.
 *
 * These correspond to the cases of Apple's
 * [XCUIElement.ElementType](https://developer.apple.com/documentation/xcuiautomation/xcuielement/elementtype)
 * enumeration. Use them with [nsPredicate] or [iOSClassChain] to avoid
 * error-prone raw strings.
 *
 * Example:
 * ```
 * private val login by iOSNSPredicate(nsPredicate {
 *     type equalTo XCUIElementType.BUTTON
 *     label equalTo "Login"
 * })
 * ```
 */
public object XCUIElementType {
    /** Activity indicator element. */
    public const val ACTIVITY_INDICATOR: String = "XCUIElementTypeActivityIndicator"

    /** Alert element. */
    public const val ALERT: String = "XCUIElementTypeAlert"

    /** Any element. */
    public const val ANY: String = "XCUIElementTypeAny"

    /** Application element. */
    public const val APPLICATION: String = "XCUIElementTypeApplication"

    /** Browser element. */
    public const val BROWSER: String = "XCUIElementTypeBrowser"

    /** Button element. */
    public const val BUTTON: String = "XCUIElementTypeButton"

    /** Cell element. */
    public const val CELL: String = "XCUIElementTypeCell"

    /** Check box element. */
    public const val CHECK_BOX: String = "XCUIElementTypeCheckBox"

    /** Collection view element. */
    public const val COLLECTION_VIEW: String = "XCUIElementTypeCollectionView"

    /** Color well element. */
    public const val COLOR_WELL: String = "XCUIElementTypeColorWell"

    /** Combo box element. */
    public const val COMBO_BOX: String = "XCUIElementTypeComboBox"

    /** Date picker element. */
    public const val DATE_PICKER: String = "XCUIElementTypeDatePicker"

    /** Decrement arrow element. */
    public const val DECREMENT_ARROW: String = "XCUIElementTypeDecrementArrow"

    /** Dialog element. */
    public const val DIALOG: String = "XCUIElementTypeDialog"

    /** Disclosure triangle element. */
    public const val DISCLOSURE_TRIANGLE: String = "XCUIElementTypeDisclosureTriangle"

    /** Dock item element. */
    public const val DOCK_ITEM: String = "XCUIElementTypeDockItem"

    /** Drawer element. */
    public const val DRAWER: String = "XCUIElementTypeDrawer"

    /** Grid element. */
    public const val GRID: String = "XCUIElementTypeGrid"

    /** Group element. */
    public const val GROUP: String = "XCUIElementTypeGroup"

    /** Handle element. */
    public const val HANDLE: String = "XCUIElementTypeHandle"

    /** Help tag element. */
    public const val HELP_TAG: String = "XCUIElementTypeHelpTag"

    /** Icon element. */
    public const val ICON: String = "XCUIElementTypeIcon"

    /** Image element. */
    public const val IMAGE: String = "XCUIElementTypeImage"

    /** Increment arrow element. */
    public const val INCREMENT_ARROW: String = "XCUIElementTypeIncrementArrow"

    /** Key element. */
    public const val KEY: String = "XCUIElementTypeKey"

    /** Keyboard element. */
    public const val KEYBOARD: String = "XCUIElementTypeKeyboard"

    /** Layout area element. */
    public const val LAYOUT_AREA: String = "XCUIElementTypeLayoutArea"

    /** Layout item element. */
    public const val LAYOUT_ITEM: String = "XCUIElementTypeLayoutItem"

    /** Level indicator element. */
    public const val LEVEL_INDICATOR: String = "XCUIElementTypeLevelIndicator"

    /** Link element. */
    public const val LINK: String = "XCUIElementTypeLink"

    /** Map element. */
    public const val MAP: String = "XCUIElementTypeMap"

    /** Matte element. */
    public const val MATTE: String = "XCUIElementTypeMatte"

    /** Menu element. */
    public const val MENU: String = "XCUIElementTypeMenu"

    /** Menu bar element. */
    public const val MENU_BAR: String = "XCUIElementTypeMenuBar"

    /** Menu bar item element. */
    public const val MENU_BAR_ITEM: String = "XCUIElementTypeMenuBarItem"

    /** Menu button element. */
    public const val MENU_BUTTON: String = "XCUIElementTypeMenuButton"

    /** Menu item element. */
    public const val MENU_ITEM: String = "XCUIElementTypeMenuItem"

    /** Navigation bar element. */
    public const val NAVIGATION_BAR: String = "XCUIElementTypeNavigationBar"

    /** Other element. */
    public const val OTHER: String = "XCUIElementTypeOther"

    /** Outline element. */
    public const val OUTLINE: String = "XCUIElementTypeOutline"

    /** Outline row element. */
    public const val OUTLINE_ROW: String = "XCUIElementTypeOutlineRow"

    /** Page indicator element. */
    public const val PAGE_INDICATOR: String = "XCUIElementTypePageIndicator"

    /** Picker element. */
    public const val PICKER: String = "XCUIElementTypePicker"

    /** Picker wheel element. */
    public const val PICKER_WHEEL: String = "XCUIElementTypePickerWheel"

    /** Pop-up button element. */
    public const val POP_UP_BUTTON: String = "XCUIElementTypePopUpButton"

    /** Popover element. */
    public const val POPOVER: String = "XCUIElementTypePopover"

    /** Progress indicator element. */
    public const val PROGRESS_INDICATOR: String = "XCUIElementTypeProgressIndicator"

    /** Radio button element. */
    public const val RADIO_BUTTON: String = "XCUIElementTypeRadioButton"

    /** Radio group element. */
    public const val RADIO_GROUP: String = "XCUIElementTypeRadioGroup"

    /** Rating indicator element. */
    public const val RATING_INDICATOR: String = "XCUIElementTypeRatingIndicator"

    /** Relevance indicator element. */
    public const val RELEVANCE_INDICATOR: String = "XCUIElementTypeRelevanceIndicator"

    /** Ruler element. */
    public const val RULER: String = "XCUIElementTypeRuler"

    /** Ruler marker element. */
    public const val RULER_MARKER: String = "XCUIElementTypeRulerMarker"

    /** Scroll bar element. */
    public const val SCROLL_BAR: String = "XCUIElementTypeScrollBar"

    /** Scroll view element. */
    public const val SCROLL_VIEW: String = "XCUIElementTypeScrollView"

    /** Search field element. */
    public const val SEARCH_FIELD: String = "XCUIElementTypeSearchField"

    /** Secure text field element. */
    public const val SECURE_TEXT_FIELD: String = "XCUIElementTypeSecureTextField"

    /** Segmented control element. */
    public const val SEGMENTED_CONTROL: String = "XCUIElementTypeSegmentedControl"

    /** Sheet element. */
    public const val SHEET: String = "XCUIElementTypeSheet"

    /** Slider element. */
    public const val SLIDER: String = "XCUIElementTypeSlider"

    /** Split group element. */
    public const val SPLIT_GROUP: String = "XCUIElementTypeSplitGroup"

    /** Splitter element. */
    public const val SPLITTER: String = "XCUIElementTypeSplitter"

    /** Static text element. */
    public const val STATIC_TEXT: String = "XCUIElementTypeStaticText"

    /** Status bar element. */
    public const val STATUS_BAR: String = "XCUIElementTypeStatusBar"

    /** Status item element. */
    public const val STATUS_ITEM: String = "XCUIElementTypeStatusItem"

    /** Stepper element. */
    public const val STEPPER: String = "XCUIElementTypeStepper"

    /** Switch element. */
    public const val SWITCH: String = "XCUIElementTypeSwitch"

    /** Tab element. */
    public const val TAB: String = "XCUIElementTypeTab"

    /** Tab bar element. */
    public const val TAB_BAR: String = "XCUIElementTypeTabBar"

    /** Tab group element. */
    public const val TAB_GROUP: String = "XCUIElementTypeTabGroup"

    /** Table element. */
    public const val TABLE: String = "XCUIElementTypeTable"

    /** Table column element. */
    public const val TABLE_COLUMN: String = "XCUIElementTypeTableColumn"

    /** Table row element. */
    public const val TABLE_ROW: String = "XCUIElementTypeTableRow"

    /** Text field element. */
    public const val TEXT_FIELD: String = "XCUIElementTypeTextField"

    /** Text view element. */
    public const val TEXT_VIEW: String = "XCUIElementTypeTextView"

    /** Timeline element. */
    public const val TIMELINE: String = "XCUIElementTypeTimeline"

    /** Toggle element. */
    public const val TOGGLE: String = "XCUIElementTypeToggle"

    /** Toolbar element. */
    public const val TOOLBAR: String = "XCUIElementTypeToolbar"

    /** Toolbar button element. */
    public const val TOOLBAR_BUTTON: String = "XCUIElementTypeToolbarButton"

    /** Touch bar element. */
    public const val TOUCH_BAR: String = "XCUIElementTypeTouchBar"

    /** Value indicator element. */
    public const val VALUE_INDICATOR: String = "XCUIElementTypeValueIndicator"

    /** Web view element. */
    public const val WEB_VIEW: String = "XCUIElementTypeWebView"

    /** Window element. */
    public const val WINDOW: String = "XCUIElementTypeWindow"
}
