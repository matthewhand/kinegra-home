package tv.metaxy.dashboard;

/**
 * Deterministic TV remote focus model: six apps in two 3-column rows,
 * then two system tools in a final row. No Android dependencies.
 *
 * Every direction has bounded candidates; never wrap into settings
 * accidentally on LEFT/RIGHT or when the current row is exhausted.
 * A destination must be enabled. Returning -1 means "no movement".
 */
public final class RemoteFocusPolicy {
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 3;
    public static final int DOWN = 4;
    public static final int TARGET_COUNT = 8;

    private static final int[][] COLUMNS = {{0, 1, 2}, {3, 4, 5}, {6, 7}};
    private RemoteFocusPolicy() { }

    public static int firstEnabled(boolean[] enabled) {
        requireEight(enabled);
        for (int i = 0; i < enabled.length; ++i) if (enabled[i]) return i;
        return -1;
    }

    public static int restore(int desired, boolean[] enabled) {
        requireEight(enabled);
        if (desired >= 0 && desired < enabled.length && enabled[desired])
            return desired;
        return firstEnabled(enabled);
    }

    public static int next(int from, int direction, boolean[] enabled) {
        requireEight(enabled);
        if (from < 0 || from >= TARGET_COUNT || !enabled[from])
            return firstEnabled(enabled);
        int row = from / 3;
        int col = from % 3;
        if (row == 2) col = from - 6;

        if (direction == LEFT || direction == RIGHT) {
            int[] cells = COLUMNS[row];
            int step = direction == LEFT ? -1 : 1;
            for (int j = col + step; j >= 0 && j < cells.length; j += step)
                if (enabled[cells[j]]) return cells[j];
            return -1;
        }
        if (direction == UP || direction == DOWN) {
            int nextRow = row + (direction == UP ? -1 : 1);
            if (nextRow < 0 || nextRow >= COLUMNS.length) return -1;
            int[] candidates = COLUMNS[nextRow];
            // Always pick nearest horizontal column, and prefer lower
            // column on ties. We never skip whole rows unexpectedly.
            for (int distance = 0; distance < 3; distance++) {
                int left = col - distance;
                if (left >= 0 && left < candidates.length
                        && enabled[candidates[left]]) return candidates[left];
                int right = col + distance;
                if (distance != 0 && right < candidates.length
                        && enabled[candidates[right]]) return candidates[right];
            }
        }
        return -1;
    }

    /** Two-column TV home; skip wholly empty recent rows to keep tools reachable. */
    public static int next(int from, int direction, boolean[] enabled, int columns) {
        if (columns == 3) return next(from, direction, enabled);
        if (columns != 2) throw new IllegalArgumentException("Expected two or three columns");
        requireEight(enabled);
        if(enabled.length==10){
            if(from<0||from>=10||!enabled[from])return firstEnabled(enabled);
            int row=from>=8?0:from/2+1,col=from%2;
            if(direction==LEFT||direction==RIGHT){int next=from+(direction==LEFT?-1:1);return next>=0&&next<10&&next%2!=col&&((from>=8)==(next>=8))&&(from>=8||next/2==from/2)&&enabled[next]?next:-1;}
            if(direction==UP||direction==DOWN){int step=direction==UP?-1:1;for(int r=row+step;r>=0&&r<5;r+=step){int preferred=(r==0?8:(r-1)*2)+col,other=(r==0?8:(r-1)*2)+1-col;if(enabled[preferred])return preferred;if(enabled[other])return other;}}
            return -1;
        }
        if (from < 0 || from >= TARGET_COUNT || !enabled[from]) return firstEnabled(enabled);
        int row=from/2,col=from%2;
        if(direction==LEFT||direction==RIGHT){int target=from+(direction==LEFT?-1:1);return target>=0&&target<TARGET_COUNT&&target/2==row&&enabled[target]?target:-1;}
        if(direction==UP||direction==DOWN){int step=direction==UP?-1:1;for(int r=row+step;r>=0&&r<4;r+=step){int preferred=r*2+col,other=r*2+1-col;if(enabled[preferred])return preferred;if(enabled[other])return other;}}
        return -1;
    }

    private static void requireEight(boolean[] enabled) {
        if (enabled == null || (enabled.length != TARGET_COUNT && enabled.length != 10))
            throw new IllegalArgumentException("Expected exactly 8 focus cells");
    }
}
