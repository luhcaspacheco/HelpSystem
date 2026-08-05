import type { CSSProperties, ReactNode } from 'react';
import './styles.css';

interface TextProps {
    className?: 'text' | 'title' | 'subtitle';
    style?: CSSProperties;
    children: ReactNode;
}

export default function Text({ style, children, className = 'text' }: TextProps) {
  return (
    <div style={style} className={`text-component ${className}`}>
      {children}
    </div>
  );
}