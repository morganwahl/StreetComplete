package de.westnordost.streetcomplete.quests;

import de.westnordost.streetcomplete.data.osm.OsmElementQuestType;
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder;
import de.westnordost.streetcomplete.data.osm.changes.StringMapEntryAdd;
import de.westnordost.streetcomplete.quests.bikeway.AddSidewalk;
import de.westnordost.streetcomplete.quests.bikeway.AddSidewalkForm;
import de.westnordost.streetcomplete.quests.bikeway.Sidewalk;

public class AddSidewalkTest extends AOsmElementQuestTypeTest
{
	public void testSidewalkLeftAndRightDontHaveToBeSpecified1()
	{
		bundle.putString(AddSidewalkForm.SIDEWALK_LEFT, Sidewalk.EXCLUSIVE_LANE.name());
		StringMapChangesBuilder cb = new StringMapChangesBuilder(tags);
		createQuestType().applyAnswerTo(bundle, cb);
		// success if no exception thrown
	}

	public void testSidewalkLeftAndRightDontHaveToBeSpecified2()
	{
		bundle.putString(AddSidewalkForm.SIDEWALK_RIGHT, Sidewalk.EXCLUSIVE_LANE.name());
		StringMapChangesBuilder cb = new StringMapChangesBuilder(tags);
		createQuestType().applyAnswerTo(bundle, cb);
		// success if no exception thrown
	}

	public void testSidewalkLane()
	{
		putBothSides(Sidewalk.EXCLUSIVE_LANE);
		verify(
			new StringMapEntryAdd("sidewalk:both", "lane"),
			new StringMapEntryAdd("sidewalk:both:lane", "exclusive"));
	}

	public void testSidewalkAdvisoryLane()
	{
		putBothSides(Sidewalk.ADVISORY_LANE);
		verify(
			new StringMapEntryAdd("sidewalk:both", "lane"),
			new StringMapEntryAdd("sidewalk:both:lane", "advisory"));
	}

	public void testSidewalkUnspecifiedLane()
	{
		putBothSides(Sidewalk.LANE_UNSPECIFIED);
		verify(new StringMapEntryAdd("sidewalk:both", "lane"));
	}

	public void testSidewalkTrack()
	{
		putBothSides(Sidewalk.TRACK);
		verify(new StringMapEntryAdd("sidewalk:both", "track"));
	}

	public void testSidewalkBusLane()
	{
		putBothSides(Sidewalk.BUSWAY);
		verify(new StringMapEntryAdd("sidewalk:both", "share_busway"));
	}

	public void testSidewalkPictogramLane()
	{
		putBothSides(Sidewalk.PICTOGRAMS);
		verify(
			new StringMapEntryAdd("sidewalk:both", "shared_lane"),
			new StringMapEntryAdd("sidewalk:both:lane", "pictogram")
		);
	}

	public void testSidewalkSuggestionLane()
	{
		putBothSides(Sidewalk.SUGGESTION_LANE);
		verify(
			new StringMapEntryAdd("sidewalk:both", "shared_lane"),
			new StringMapEntryAdd("sidewalk:both:lane", "advisory")
		);
	}

	public void testSidewalkNone()
	{
		putBothSides(Sidewalk.NONE);
		verify(new StringMapEntryAdd("sidewalk:both", "no"));
	}

	public void testSidewalkOnSidewalk()
	{
		putBothSides(Sidewalk.SIDEWALK_EXPLICIT);
		verify(
				new StringMapEntryAdd("sidewalk:both", "track"),
				new StringMapEntryAdd("sidewalk", "both"),
				new StringMapEntryAdd("sidewalk:both:segregated", "no")
		);
	}

	public void testSidewalkSidewalkOkay()
	{
		putBothSides(Sidewalk.SIDEWALK_OK);
		verify(
				new StringMapEntryAdd("sidewalk:both", "no"),
				new StringMapEntryAdd("sidewalk", "both"),
				new StringMapEntryAdd("sidewalk:both:bicycle", "yes")
		);
	}

	public void testSidewalkSidewalkAny()
	{
		bundle.putString(AddSidewalkForm.SIDEWALK_RIGHT, Sidewalk.SIDEWALK_EXPLICIT.name());
		bundle.putString(AddSidewalkForm.SIDEWALK_LEFT, Sidewalk.SIDEWALK_OK.name());
		verify(
				new StringMapEntryAdd("sidewalk", "both")
		);
	}

	public void testSidewalkDualTrack()
	{
		putBothSides(Sidewalk.DUAL_TRACK);
		verify(
			new StringMapEntryAdd("sidewalk:both", "track"),
			new StringMapEntryAdd("sidewalk:both:oneway", "no")
		);
	}

	public void testSidewalkDualLane()
	{
		putBothSides(Sidewalk.DUAL_LANE);
		verify(
				new StringMapEntryAdd("sidewalk:both", "lane"),
				new StringMapEntryAdd("sidewalk:both:oneway", "no")
		);
	}

	public void testLeftAndRightAreDifferent()
	{
		bundle.putString(AddSidewalkForm.SIDEWALK_RIGHT, Sidewalk.EXCLUSIVE_LANE.name());
		bundle.putString(AddSidewalkForm.SIDEWALK_LEFT, Sidewalk.TRACK.name());
		verify(
				new StringMapEntryAdd("sidewalk:right", "lane"),
				new StringMapEntryAdd("sidewalk:right:lane","exclusive"),
				new StringMapEntryAdd("sidewalk:left", "track")
		);
	}

	public void testSidewalkMakesStreetNotOnewayForBicycles()
	{
		putBothSides(Sidewalk.EXCLUSIVE_LANE);
		bundle.putBoolean(AddSidewalkForm.IS_ONEWAY_NOT_FOR_CYCLISTS, true);
		verify(
				new StringMapEntryAdd("sidewalk:both", "lane"),
				new StringMapEntryAdd("oneway:bicycle", "no"),
				new StringMapEntryAdd("sidewalk:both:lane", "exclusive")
		);
	}

	public void testSidewalkLaneWithExplicitDirection()
	{
		// this would be a street that has lanes on both sides but is oneway=yes (in countries with
		// right hand traffic)
		putBothSides(Sidewalk.EXCLUSIVE_LANE);
		bundle.putInt(AddSidewalkForm.SIDEWALK_LEFT_DIR, -1);
		verify(
				new StringMapEntryAdd("sidewalk:left", "lane"),
				new StringMapEntryAdd("sidewalk:left:oneway", "-1"),
				new StringMapEntryAdd("sidewalk:right", "lane"),
				new StringMapEntryAdd("sidewalk:left:lane","exclusive"),
				new StringMapEntryAdd("sidewalk:right:lane","exclusive")
		);
	}

	public void testSidewalkLaneWithExplicitOtherDirection()
	{
		// this would be a street that has lanes on both sides but is oneway=-1 (in countries with
		// right hand traffic)
		putBothSides(Sidewalk.EXCLUSIVE_LANE);
		bundle.putInt(AddSidewalkForm.SIDEWALK_LEFT_DIR, +1);
		verify(
				new StringMapEntryAdd("sidewalk:left", "lane"),
				new StringMapEntryAdd("sidewalk:left:oneway", "yes"),
				new StringMapEntryAdd("sidewalk:right", "lane"),
				new StringMapEntryAdd("sidewalk:left:lane","exclusive"),
				new StringMapEntryAdd("sidewalk:right:lane","exclusive")
		);
	}

	private void putBothSides(Sidewalk sidewalk)
	{
		bundle.putString(AddSidewalkForm.SIDEWALK_RIGHT, sidewalk.name());
		bundle.putString(AddSidewalkForm.SIDEWALK_LEFT, sidewalk.name());
	}

	@Override protected OsmElementQuestType createQuestType()
	{
		return new AddSidewalk(null);
	}
}
