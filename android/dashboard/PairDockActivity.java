package tv.metaxy.proof;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import tv.metaxy.dashboard.PairGeometry;
import tv.metaxy.dashboard.PairProtocol;

/** Compact controls in a third, non-overlapping freeform window. */
public final class PairDockActivity extends PairUi {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(BG);scroll.setFillViewport(true);
        LinearLayout body=new LinearLayout(this);body.setOrientation(1);body.setPadding(dp(8),dp(4),dp(8),dp(4));scroll.addView(body);
        connection=text("Kinegra Home controls",13,ACCENT);body.addView(connection);
        HorizontalScrollView viewport=new HorizontalScrollView(this);viewport.setFillViewport(true);viewport.setFocusable(false);body.addView(viewport);
        LinearLayout row=new LinearLayout(this);row.setOrientation(0);viewport.addView(row);
        add(row,"Focus left",()->action(PairProtocol.LEFT));add(row,"Focus right",()->action(PairProtocol.RIGHT));
        add(row,"Swap",()->{String left=state.left;state.left=state.right;state.right=left;action(PairProtocol.APPLY);});
        add(row,"Layout",()->{state.mode=(state.mode+1)%PairGeometry.NAMES.length;action(PairProtocol.APPLY);});
        add(row,"Smaller",()->{state.size=Math.max(60,state.size-10);action(PairProtocol.APPLY);});
        add(row,"Larger",()->{state.size=Math.min(100,state.size+10);action(PairProtocol.APPLY);});
        add(row,"Full left",()->action(PairProtocol.FULL_LEFT));add(row,"Full right",()->action(PairProtocol.FULL_RIGHT));
        add(row,"Choose apps",()->action(PairProtocol.EDIT));add(row,"End layout",()->action(PairProtocol.END));
        message=text(PairGeometry.NAMES[state.mode]+"  "+state.size+"%  |  Home, then Kinegra Home returns to these controls",12,MUTED);body.addView(message);
        setContentView(scroll);row.getChildAt(0).requestFocus();
    }
    private void add(LinearLayout row,String label,Runnable action){Button b=button(label,action);b.setTextSize(14);b.setMinHeight(dp(46));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(dp(3),0,dp(3),0);row.addView(b,p);}
    @Override void afterAction(int operation,boolean ok){if(ok&&operation==PairProtocol.APPLY){state=new PairState(this);message.setText(PairGeometry.NAMES[state.mode]+"  "+state.size+"%  |  Home, then Kinegra Home returns to controls");}}
}
