package com.brstf.simplelife;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.brstf.simplelife.dialog.AboutDialog;

import java.util.Random;

public class SettingsFragment extends Fragment implements AnimationListener {
	private TextView m_invertText;
	private Animation m_anim1;
	private Animation m_anim2;
	private CheckBox m_invert_cb;
	private CheckBox m_poison_cb;
	private CheckBox m_wake_cb;
	private CheckBox m_quick_cb;
	private CheckBox m_bigmod_cb;
	private CheckBox m_custombg_cb;
	private SharedPreferences mPrefs;
	private Random m_rand;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_settings, container, false);
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		m_rand = new Random(System.currentTimeMillis());

		// Get the shared preferences
		mPrefs = ((LifeCount) getActivity()).getPrefs();

		// Add click listeners to the reset buttons
		ImageButton reset = (ImageButton) getView()
				.findViewById(R.id.but_reset);
		reset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reset(mPrefs.getInt(
						getActivity().getString(R.string.key_total), 20));
			}
		});

		int resetval = mPrefs.getInt(getActivity()
				.getString(R.string.key_total), 20);
		changeResetVal(resetval);

		final RadioGroup reset_radio = (RadioGroup) getView().findViewById(
				R.id.settings_reset_val_rg);
		switch (resetval) {
		case 20:
			reset_radio.check(R.id.settings_rg_20);
			break;
		case 40:
			reset_radio.check(R.id.settings_rg_40);
			break;
		default:
			reset_radio.check(R.id.settings_rg_custom);
			break;
		}

		reset_radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Log.d("RESET RADIO", Integer.toString(checkedId));
				switch (checkedId) {
				case R.id.settings_rg_20:
					changeResetVal(20);
					break;
				case R.id.settings_rg_40:
					changeResetVal(40);
					break;
				case R.id.settings_rg_custom:
					changeResetValCustom();

					// TODO: see whether removing this breaks anything
					/*
					getActivity().setTheme(R.style.AppBaseThemeBlack);
					getActivity().recreate();
					*/
					break;
				}
			}
		});

		// Set Poison Check Box Listener
		m_poison_cb = (CheckBox) getView().findViewById(R.id.poison_check);
		boolean showPoison = mPrefs.getBoolean(getString(R.string.key_poison),
				true);
		m_poison_cb.setChecked(showPoison);
		Button but_poison = (Button) getView().findViewById(R.id.but_poison);
		but_poison.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_poison_cb.setChecked(!m_poison_cb.isChecked());
				mPrefs.edit()
						.putBoolean(
								getActivity().getString(R.string.key_poison),
								m_poison_cb.isChecked()).apply();
			}
		});

		// Load the flip animations
		m_anim1 = AnimationUtils.loadAnimation(this.getActivity(),
				R.animator.to_middle_vert);
		m_anim1.setAnimationListener(this);
		m_anim2 = AnimationUtils.loadAnimation(this.getActivity(),
				R.animator.from_middle_vert);
		m_anim2.setAnimationListener(this);

		// Set the check button listener
		m_invert_cb = (CheckBox) getView().findViewById(
				R.id.settings_invert_check);
		boolean inverted = mPrefs.getBoolean(getString(R.string.key_invert),
				true);
		m_invertText = (TextView) getView().findViewById(R.id.invert_tv);
		m_invertText.setRotation(inverted ? 180.0f : 0.0f);
		m_invert_cb.setChecked(inverted);
		Button but_invert = (Button) this.getView().findViewById(
				R.id.but_invert);
		but_invert.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				m_invert_cb.setChecked(!m_invert_cb.isChecked());
				m_invertText.setAnimation(m_anim1);
				m_invertText.startAnimation(m_anim1);
				mPrefs.edit()
						.putBoolean(
								getActivity().getString(R.string.key_invert),
								m_invert_cb.isChecked()).apply();
			}
		});

		// Setup Dice Rolling listeners
		setDiceSidesText(mPrefs.getInt(getString(R.string.key_dice_sides), 6));
		((Button) getView().findViewById(R.id.dice_sides_but_up))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						changeDiceSides(1);
					}
				});
		((Button) getView().findViewById(R.id.dice_sides_but_down))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						changeDiceSides(-1);
					}
				});
		setDiceNumText(mPrefs.getInt(getString(R.string.key_dice_num), 2));
		((Button) getView().findViewById(R.id.dice_num_but_up))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						changeDiceNum(1);
					}
				});
		((Button) getView().findViewById(R.id.dice_num_but_down))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						changeDiceNum(-1);
					}
				});
		((Button) getView().findViewById(R.id.but_dice_roll))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						int sides = mPrefs.getInt(
								getString(R.string.key_dice_sides), 6);
						int num = mPrefs.getInt(
								getString(R.string.key_dice_num), 2);
						rollDice(sides, num);
					}
				});

		// Set wake lock change listener
		m_wake_cb = (CheckBox) getView().findViewById(R.id.settings_wake_check);
		m_wake_cb.setChecked(mPrefs.getBoolean(getString(R.string.key_wake),
				true));
		Button but_wake = (Button) getView().findViewById(R.id.but_wake);
		but_wake.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_wake_cb.setChecked(!m_wake_cb.isChecked());
				mPrefs.edit()
						.putBoolean(getActivity().getString(R.string.key_wake),
								m_wake_cb.isChecked()).apply();
			}
		});

		// Set quick reset change listener
		m_quick_cb = (CheckBox) getView().findViewById(
				R.id.settings_quick_check);
		m_quick_cb.setChecked(mPrefs.getBoolean(getString(R.string.key_quick),
				false));
		Button but_quick = (Button) getView().findViewById(R.id.but_quick);
		but_quick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_quick_cb.setChecked(!m_quick_cb.isChecked());
				mPrefs.edit()
						.putBoolean(
								getActivity().getString(R.string.key_quick),
								m_quick_cb.isChecked()).apply();

				if (m_quick_cb.isChecked()) {
					Toast.makeText(
							getActivity(),
							"Quick-Reset enabled. Tap settings button to reset life "
									+ "totals, long-press it to show settings",
							Toast.LENGTH_LONG).show();
				}
			}
		});

		// Set entry time
		setEntryText(mPrefs.getFloat(getString(R.string.key_entry), 2.0f));
		((Button) getView().findViewById(R.id.entry_but_up))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						changeEntryTime(0.25f);
					}
				});
		((Button) getView().findViewById(R.id.entry_but_down))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						changeEntryTime(-0.25f);
					}
				});

		// Set up +5/-5 check box
		m_bigmod_cb = (CheckBox) getView().findViewById(
				R.id.settings_bigmod_check);
		m_bigmod_cb.setChecked(mPrefs.getBoolean(getString(R.string.key_bigmod),
				true));
		Button but_bigmod = (Button) getView().findViewById(R.id.but_bigmod);
		but_bigmod.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_bigmod_cb.setChecked(!m_bigmod_cb.isChecked());
				mPrefs.edit()
						.putBoolean(
								getActivity().getString(R.string.key_bigmod),
								m_bigmod_cb.isChecked()).apply();
			}
		});

		// Setup Theme Chooser
		int theme_id = mPrefs.getInt(getActivity()
				.getString(R.string.key_theme), R.style.AppBaseThemeLight);
		RadioGroup theme_radio = (RadioGroup) getView().findViewById(
				R.id.settings_theme_rg);
		theme_radio.setOnCheckedChangeListener(null);
		switch (theme_id) {
		case R.style.AppBaseThemeLight:
			theme_radio.check(R.id.settings_theme_rb_light);
			break;
		case R.style.AppBaseThemeDark:
			theme_radio.check(R.id.settings_theme_rb_dark);
			break;
		case R.style.AppBaseThemeBlack:
			theme_radio.check(R.id.settings_theme_rb_black);
			break;
		}

		theme_radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.settings_theme_rb_light:
					changeTheme(R.style.AppBaseThemeLight);
					break;
				case R.id.settings_theme_rb_dark:
					changeTheme(R.style.AppBaseThemeDark);
					break;
				case R.id.settings_theme_rb_black:
					changeTheme(R.style.AppBaseThemeBlack);
					break;
				}
			}
		});

		// Set up custom background option
		m_custombg_cb = (CheckBox) getView().findViewById(
				R.id.settings_background_check);
		m_custombg_cb.setChecked(mPrefs.getBoolean(getString(R.string.key_custombg), false));
		Button but_custombg = (Button) getView().findViewById(R.id.but_background);
		but_custombg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: check if bg actually loaded
				m_custombg_cb.setChecked(!m_custombg_cb.isChecked());
				mPrefs.edit()
						.putBoolean(
								getActivity().getString(R.string.key_custombg),
								m_custombg_cb.isChecked()).apply();

			}
		});

		Button but_loadbg = (Button) getView().findViewById(R.id.but_background_select);
		but_loadbg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// send intent or w/e to load in image
			}
		});


		// Setup about button
		ImageButton but_about = (ImageButton) this.getView().findViewById(
				R.id.but_about);
		but_about.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog about = AboutDialog.create(getActivity());
				about.show();
			}
		});

		// Setup github button
		ImageButton but_github = (ImageButton) this.getView().findViewById(
				R.id.but_github);
		but_github.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("https://github.com/brstf/SimpleLife"));
				startActivity(browserIntent);
			}
		});
	}

	/**
	 * Change the theme of the app to the given theme.
	 * 
	 * @param theme_id
	 *            Id of the theme to change the apps theme to
	 */
	private void changeTheme(int theme_id) {
		mPrefs.edit().putInt(getString(R.string.key_theme), theme_id).apply();
	}

	/**
	 * Simulate rolling of the dice by rolling the given number of dice with the
	 * given number of sides.
	 * 
	 * @param sides
	 *            Number of sides on the dice to roll
	 * @param num
	 *            Number of dice to roll
	 */
	protected void rollDice(int sides, int num) {
		int total = 0;
		for (int i = 0; i < num; ++i) {
			total += 1 + m_rand.nextInt(sides);
		}
		((TextView) getView().findViewById(R.id.settings_dice_result))
				.setText(String.valueOf(total));
	}

	/**
	 * Change the number of sides on the dice to roll by the given amount.
	 * 
	 * @param mod
	 *            Number to change the number of sides on the dice to roll by
	 */
	protected void changeDiceSides(int mod) {
		int sides = mPrefs.getInt(getString(R.string.key_dice_sides), 6);
		if (sides <= 1 && mod < 0) {
			return;
		}

		setDiceSidesText(sides + mod);
		mPrefs.edit().putInt(getString(R.string.key_dice_sides), sides + mod)
				.apply();
	}

	/**
	 * Sets the text of the number of sides on each die being rolled.
	 * 
	 * @param sides
	 *            Number of sides on the dice
	 */
	private void setDiceSidesText(int sides) {
		((TextView) getView().findViewById(R.id.settings_dice_sides_tv))
				.setText(String.valueOf(sides));
	}

	/**
	 * Change the number of dice to roll by the given amount.
	 * 
	 * @param mod
	 *            Number to change the number of dice to roll by
	 */
	protected void changeDiceNum(int mod) {
		int num = mPrefs.getInt(getString(R.string.key_dice_num), 2);
		if (num <= 1 && mod < 0) {
			return;
		}

		setDiceNumText(num + mod);
		mPrefs.edit().putInt(getString(R.string.key_dice_num), num + mod)
				.apply();
	}

	/**
	 * Sets the text of the number of dice being rolled.
	 * 
	 * @param num
	 *            Number of dice to roll
	 */
	private void setDiceNumText(int num) {
		((TextView) getView().findViewById(R.id.settings_dice_num_tv))
				.setText(String.valueOf(num));
	}

	/**
	 * Modify the time before a life modification is "locked-in" by adding the
	 * given modification to the current value. This saves the new value in the
	 * shared preferences and sets the TextView text.
	 * 
	 * @param mod
	 *            Amount to change the entry time by
	 */
	private void changeEntryTime(float mod) {
		float time = mPrefs.getFloat(getString(R.string.key_entry), 2.0f);
		if ((time <= 1.0f && mod < 0.0f) || (time >= 6.0f && mod > 0.0f)) {
			return;
		}

		setEntryText(time + mod);
		mPrefs.edit()
				.putFloat(getActivity().getString(R.string.key_entry),
						time + mod).apply();
	}

	private void setEntryText(float time) {
		((TextView) getView().findViewById(R.id.settings_entry_tv))
				.setText(String.format("%.2f", time));
	}

	protected void changeResetVal(int reset) {
		mPrefs.edit()
				.putInt(getActivity().getString(R.string.key_total), reset)
				.apply();
		((TextView) this.getView().findViewById(R.id.settings_reset_text))
				.setText(String.valueOf(reset));
	}

	/**
	 * Resets the LifeCount and pops this fragment off the stack. Note: This
	 * fragment should only ever be added to the back stack when adding this
	 * fragment.
	 * 
	 * @param resetval
	 *            Integer value to reset the life totals to
	 */
	private void reset(int resetval) {
		((LifeCount) getActivity()).reset(resetval);
		SettingsFragment.this.getFragmentManager().popBackStack();
	}

	/**
	 * Changes reset value to a user-selected custom value. Creates an
	 * AlertDialog for the user to enter their custom reset value.
	 */
	private void changeResetValCustom() {
		// AlertDialog to prompt user for custom life value
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final AlertDialog dialog;
		builder.setMessage(getString(R.string.settings_reset_custom_prompt));

		final EditText lifeInput = new EditText(getActivity());
		lifeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
		builder.setView(lifeInput);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// TODO: Double check on other devices before removing completely
				/*
				if (lifeInput.getText().toString().equals("")) {
					return;
				}
				*/
				int customTotal = Integer.parseInt(lifeInput.getText().toString());
				changeResetVal(customTotal);
				dialog.dismiss();
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				int resetval = mPrefs.getInt(getActivity()
						.getString(R.string.key_total), 20);
				switch (resetval) {
					case 20:
						RadioButton button20 = (RadioButton) getView().findViewById(R.id.settings_rg_20);
						button20.setChecked(true);
						break;
					case 40:
						RadioButton button40 = (RadioButton) getView().findViewById(R.id.settings_rg_40);
						button40.setChecked(true);
						break;
					default:
						// should be unreachable
						break;
				}
				dialog.cancel();
			}
		});

		dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();

		// disable PositiveButton until number is entered
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
		lifeInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// Check if edittext is empty
				if (lifeInput.getText().length() == 0) {
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				} else {
					dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				}
			}
		});
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (animation == m_anim1) {
			if (m_invert_cb.isChecked()) {
				m_invertText.setRotation(180.0f);
			} else {
				m_invertText.setRotation(0.0f);
			}
			m_invertText.setGravity(Gravity.CENTER);
			m_invertText.clearAnimation();
			m_invertText.startAnimation(m_anim2);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}
}
