import java.util.Objects;  // Add this import

class Node {
    int x, y;
    double gScore = Double.POSITIVE_INFINITY;
    double fScore = Double.POSITIVE_INFINITY;
    
    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return x == node.x && y == node.y;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}