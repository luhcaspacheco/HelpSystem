import type { ReactNode } from "react";
import './styles.css';

interface ButtonProps {
    children: ReactNode;
    onClick?: () => void;
    type?: 'button' | 'submit';
    className?: string;
    disabled?: boolean;
}

export default function Button({ children, onClick, className,type = 'button', disabled }: ButtonProps) {
    return <button type={type} onClick={onClick} className={`button-component ${className || ''}`} disabled={disabled}>
        {children}
    </button>
}