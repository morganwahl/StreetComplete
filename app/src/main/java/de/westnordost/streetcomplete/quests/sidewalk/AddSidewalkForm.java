package de.westnordost.streetcomplete.quests.bikeway;

import android.os.Bundle;
import android.support.annotation.AnyThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.data.osm.tql.FiltersParser;
import de.westnordost.streetcomplete.data.osm.tql.TagFilterExpression;
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment;
import de.westnordost.streetcomplete.quests.StreetSideRotater;
import de.westnordost.streetcomplete.view.ListAdapter;
import de.westnordost.streetcomplete.view.StreetSideSelectPuzzle;
import de.westnordost.streetcomplete.view.dialogs.AlertDialogBuilder;

public class AddSidewalkForm extends AbstractQuestFormAnswerFragment
{
	public static final String
			SIDEWALK_LEFT = "sidewalk_left",
			SIDEWALK_RIGHT = "sidewalk_right",
			SIDEWALK_LEFT_DIR = "sidewalk_left_opposite",
			SIDEWALK_RIGHT_DIR = "sidewalk_right_opposite",
			IS_ONEWAY_NOT_FOR_CYCLISTS = "oneway_not_for_cyclists";

	private static final String
			DEFINE_BOTH_SIDES = "define_both_sides";

	private static final TagFilterExpression LIKELY_NO_BICYCLE_CONTRAFLOW = new FiltersParser().parse(
			"ways with oneway:bicycle != no and " +
			" (oneway ~ yes|-1 and highway ~ primary|secondary|tertiary or junction=roundabout)");


	private StreetSideRotater streetSideRotater;
	StreetSideSelectPuzzle puzzle;

	private boolean isDefiningBothSides;

	private Sidewalk leftSide;
	private Sidewalk rightSide;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle inState)
	{
		View view = super.onCreateView(inflater, container, inState);
		setContentView(R.layout.quest_street_side_puzzle);

		View compassNeedle = view.findViewById(R.id.compassNeedle);

		puzzle = view.findViewById(R.id.puzzle);
		puzzle.setListener(this::showSidewalkSelectionDialog);

		streetSideRotater = new StreetSideRotater(puzzle, compassNeedle, getElementGeometry());

		initPuzzleDisplay(inState);
		initPuzzleImages(inState);

		return view;
	}

	private void initPuzzleDisplay(Bundle inState)
	{
		if(inState != null)
		{
			isDefiningBothSides = inState.getBoolean(DEFINE_BOTH_SIDES);
		}
		else
		{
			isDefiningBothSides = !LIKELY_NO_BICYCLE_CONTRAFLOW.matches(getOsmElement());
		}

		if(!isDefiningBothSides)
		{
			if(isLeftHandTraffic()) puzzle.showOnlyLeftSide();
			else                    puzzle.showOnlyRightSide();

			addOtherAnswer(R.string.quest_sidewalk_answer_contraflow_sidewalk, this::showBothSides);
		}
	}

	private void initPuzzleImages(Bundle inState)
	{
		int defaultResId = isLeftHandTraffic() ?
				R.drawable.ic_sidewalk_unknown_l : R.drawable.ic_sidewalk_unknown;

		if(inState != null)
		{
			String rightSideString = inState.getString(SIDEWALK_RIGHT);
			if(rightSideString != null)
			{
				rightSide = Sidewalk.valueOf(rightSideString);
				puzzle.setRightSideImageResource(rightSide.getIconResId(isLeftHandTraffic()));
			}
			else
			{
				puzzle.setRightSideImageResource(defaultResId);
			}
			String leftSideString = inState.getString(SIDEWALK_LEFT);
			if(leftSideString != null)
			{
				leftSide = Sidewalk.valueOf(leftSideString);
				puzzle.setLeftSideImageResource(leftSide.getIconResId(isLeftHandTraffic()));
			}
			else
			{
				puzzle.setLeftSideImageResource(defaultResId);
			}
		}
		else
		{
			puzzle.setLeftSideImageResource(defaultResId);
			puzzle.setRightSideImageResource(defaultResId);
		}
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if(rightSide != null) outState.putString(SIDEWALK_RIGHT, rightSide.name());
		if(leftSide != null)  outState.putString(SIDEWALK_LEFT, leftSide.name());
		outState.putBoolean(DEFINE_BOTH_SIDES, isDefiningBothSides);
	}

	@AnyThread public void onMapOrientation(final float rotation, final float tilt)
	{
		if(streetSideRotater != null) {
			streetSideRotater.onMapOrientation(rotation, tilt);
		}
	}

	@Override protected void onClickOk()
	{
		if(leftSide == null && rightSide == null)
		{
			Toast.makeText(getActivity(), R.string.no_changes, Toast.LENGTH_SHORT).show();
			return;
		}
		else if(isDefiningBothSides && (leftSide == null || rightSide == null))
		{
			Toast.makeText(getActivity(), R.string.need_specify_both_sides, Toast.LENGTH_SHORT).show();
			return;
		}

		boolean isOnewayNotForCyclists = false;

		// a sidewalk that goes into opposite direction of a oneway street needs special tagging
		Bundle bundle = new Bundle();
		if(isOneway() && leftSide != null && rightSide != null)
		{
			// if the road is oneway=-1, a sidewalk that goes opposite to it would be sidewalk:oneway=yes
			int reverseDir = isReversedOneway() ? 1 : -1;

			if(isReverseSideRight())
			{
				if(isSingleTrackOrLane(rightSide))
				{
					bundle.putInt(SIDEWALK_RIGHT_DIR, reverseDir);
				}
				isOnewayNotForCyclists = rightSide != Sidewalk.NONE;
			}
			else
			{
				if(isSingleTrackOrLane(leftSide))
				{
					bundle.putInt(SIDEWALK_LEFT_DIR, reverseDir);
				}
				isOnewayNotForCyclists = leftSide != Sidewalk.NONE;
			}

			isOnewayNotForCyclists |= isDualTrackOrLane(leftSide);
			isOnewayNotForCyclists |= isDualTrackOrLane(rightSide);
		}

		if(leftSide != null)  bundle.putString(SIDEWALK_LEFT, leftSide.name());
		if(rightSide != null) bundle.putString(SIDEWALK_RIGHT, rightSide.name());
		bundle.putBoolean(IS_ONEWAY_NOT_FOR_CYCLISTS, isOnewayNotForCyclists);
		applyFormAnswer(bundle);
	}

	private static boolean isSingleTrackOrLane(Sidewalk sidewalk)
	{
		return sidewalk == Sidewalk.TRACK || sidewalk == Sidewalk.EXCLUSIVE_LANE;
	}

	private static boolean isDualTrackOrLane(Sidewalk sidewalk)
	{
		return sidewalk == Sidewalk.DUAL_TRACK || sidewalk == Sidewalk.DUAL_LANE;
	}

	@Override public boolean hasChanges()
	{
		return leftSide != null || rightSide != null;
	}

	private void showSidewalkSelectionDialog(final boolean isRight)
	{
		RecyclerView recyclerView = new RecyclerView(getActivity());
		recyclerView.setLayoutParams(new RecyclerView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

		final AlertDialog alertDialog = new AlertDialogBuilder(getActivity())
				.setTitle(R.string.quest_select_hint)
				.setView(recyclerView)
				.create();

		recyclerView.setAdapter(createAdapter(getSidewalkItems(isRight), sidewalk ->
		{
			alertDialog.dismiss();

			int iconResId = sidewalk.getIconResId(isLeftHandTraffic());

			if (isRight)
			{
				puzzle.replaceRightSideImageResource(iconResId);
				rightSide = sidewalk;
			}
			else
			{
				puzzle.replaceLeftSideImageResource(iconResId);
				leftSide = sidewalk;
			}
		}));
		alertDialog.show();
	}

	private List<Sidewalk> getSidewalkItems(boolean isRight)
	{
		List<Sidewalk> values = new ArrayList<>(Arrays.asList(Sidewalk.getDisplayValues()));
		// different wording for a contraflow lane that is marked like a "shared" lane (just bicycle pictogram)
		if(isOneway() && isReverseSideRight() == isRight)
		{
			Collections.replaceAll(values, Sidewalk.PICTOGRAMS, Sidewalk.NONE_NO_ONEWAY);
		}
		String country = getCountryInfo().getCountryCode();
		if("BE".equals(country))
		{
			// Belgium does not make a difference between continuous and dashed lanes -> so don't tag that difference
			// also, in Belgium there is a differentiation between the normal lanes and suggestion lanes
			values.remove(Sidewalk.EXCLUSIVE_LANE);
			values.remove(Sidewalk.ADVISORY_LANE);
			values.add(0, Sidewalk.LANE_UNSPECIFIED);
			values.add(1, Sidewalk.SUGGESTION_LANE);
		}
		else if("NL".equals(country))
		{
			// a differentiation between dashed lanes and suggestion lanes only exist in NL and BE
			values.add(values.indexOf(Sidewalk.ADVISORY_LANE)+1, Sidewalk.SUGGESTION_LANE);
		}

		return values;
	}

	private interface OnSidewalkSelected { void onSidewalkSelected(Sidewalk sidewalk); }
	private ListAdapter<Sidewalk> createAdapter(List<Sidewalk> items, final OnSidewalkSelected callback)
	{
		return new ListAdapter<Sidewalk>(items)
		{
			@Override public ViewHolder<Sidewalk> onCreateViewHolder(ViewGroup parent, int viewType)
			{
				return new ViewHolder<Sidewalk>(LayoutInflater.from(parent.getContext()).inflate(
						R.layout.labeled_icon_button_cell, parent, false))
				{
					@Override protected void onBind(final Sidewalk item)
					{
						ImageView iconView = itemView.findViewById(R.id.imageView);
						TextView textView = itemView.findViewById(R.id.textView);
						int resId = item.getIconResId(isLeftHandTraffic());
						iconView.setImageDrawable(getCurrentCountryResources().getDrawable(resId));
						textView.setText(item.nameResId);
						itemView.setOnClickListener(view -> callback.onSidewalkSelected(item));
					}
				};
			}
		};
	}

	private void showBothSides()
	{
		isDefiningBothSides = true;
		puzzle.showBothSides();
	}

	private boolean isOneway()
	{
		Map<String, String> tags = getOsmElement().getTags();
		String oneway = tags.get("oneway");
		return oneway != null && (oneway.equals("yes") || oneway.equals("-1"));
	}

	/** @return whether the side that goes into the opposite direction of the driving direction of a
	 *          one-way is on the right side of the way */
	private boolean isReverseSideRight()
	{
		return isReversedOneway() ^ isLeftHandTraffic();
	}

	private boolean isReversedOneway()
	{
		return "-1".equals(getOsmElement().getTags().get("oneway"));
	}

	// just a shortcut
	private boolean isLeftHandTraffic()
	{
		return getCountryInfo().isLeftHandTraffic();
	}
}