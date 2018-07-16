package de.westnordost.streetcomplete.quests.bikeway;

import de.westnordost.streetcomplete.R;

public enum Sidewalk
{
	// some kind of cycle lane, not specified if with continuous or dashed lane markings
	LANE_UNSPECIFIED   ( R.drawable.ic_sidewalk_lane,       R.drawable.ic_sidewalk_lane_l,        R.string.quest_sidewalk_value_lane),
	// a.k.a. exclusive lane, dedicated lane or simply (proper) lane
	EXCLUSIVE_LANE     ( R.drawable.ic_sidewalk_lane,       R.drawable.ic_sidewalk_lane_l,       R.string.quest_sidewalk_value_lane ),
	// a.k.a. protective lane, multipurpose lane, soft lane or recommended lane
	ADVISORY_LANE      ( R.drawable.ic_sidewalk_shared_lane, R.drawable.ic_sidewalk_shared_lane_l, R.string.quest_sidewalk_value_lane_soft),
	// slight difference to dashed lane only made in NL, BE
	SUGGESTION_LANE    ( R.drawable.ic_sidewalk_suggestion_lane, R.drawable.ic_sidewalk_suggestion_lane, R.string.quest_sidewalk_value_suggestion_lane),
	TRACK              ( R.drawable.ic_sidewalk_track,      R.drawable.ic_sidewalk_track_l,      R.string.quest_sidewalk_value_track ),
	NONE               ( R.drawable.ic_sidewalk_none,       R.drawable.ic_sidewalk_none,         R.string.quest_sidewalk_value_none ),
	NONE_NO_ONEWAY     ( R.drawable.ic_sidewalk_pictograms, R.drawable.ic_sidewalk_pictograms_l, R.string.quest_sidewalk_value_none_but_no_oneway ),
	PICTOGRAMS         ( R.drawable.ic_sidewalk_pictograms, R.drawable.ic_sidewalk_pictograms_l, R.string.quest_sidewalk_value_shared ),
	SIDEWALK_EXPLICIT  ( R.drawable.ic_sidewalk_sidewalk_explicit, R.drawable.ic_sidewalk_sidewalk_explicit_l,    R.string.quest_sidewalk_value_sidewalk ),
	SIDEWALK_OK        ( R.drawable.ic_sidewalk_sidewalk,   R.drawable.ic_sidewalk_sidewalk,     R.string.quest_sidewalk_value_sidewalk_allowed),
	DUAL_LANE          ( R.drawable.ic_sidewalk_lane_dual,  R.drawable.ic_sidewalk_lane_dual_l,  R.string.quest_sidewalk_value_lane_dual ),
	DUAL_TRACK         ( R.drawable.ic_sidewalk_track_dual, R.drawable.ic_sidewalk_track_dual_l, R.string.quest_sidewalk_value_track_dual ),
	BUSWAY             ( R.drawable.ic_sidewalk_bus_lane,   R.drawable.ic_sidewalk_bus_lane_l,   R.string.quest_sidewalk_value_bus_lane );

	public final int iconResId;
	public final int iconResIdLeft;
	public final int nameResId;

	// some of the values defined above are special values that should not be visible by default
	public static Sidewalk[] getDisplayValues() { return new Sidewalk[] {
		EXCLUSIVE_LANE, ADVISORY_LANE,
		TRACK, NONE,
		PICTOGRAMS, BUSWAY,
		SIDEWALK_EXPLICIT, SIDEWALK_OK,
		DUAL_LANE, DUAL_TRACK
	};}

	Sidewalk(int iconResId, int iconResIdLeft, int nameResId)
	{
		this.iconResId = iconResId;
		this.iconResIdLeft = iconResIdLeft;
		this.nameResId = nameResId;
	}

	public int getIconResId(boolean isLeftHandTraffic)
	{
		return isLeftHandTraffic ? iconResIdLeft : iconResId;
	}

	public boolean isOnSidewalk()
	{
		return this == SIDEWALK_EXPLICIT || this == SIDEWALK_OK;
	}
}
