interface CardProps {
    className?: 'card' | 'card-header' | 'card-body' | 'card-footer';
    style?: React.CSSProperties;
    children: React.ReactNode;
}


export default function Card({ style, children, className = 'card' }: CardProps) {
  return (
    <div style={style} className={`card-component ${className}`}>
      {children}
    </div>
  );
}