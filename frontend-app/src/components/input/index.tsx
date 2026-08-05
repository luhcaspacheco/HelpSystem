import type { CSSProperties } from "react";
import './styles.css';

interface InputProps {
    type?: 'text' | 'password' | 'email';
    style?: CSSProperties;
    value?: string;
    onChange?: (event: React.ChangeEvent<HTMLInputElement>) => void;
    placeholder?: string;
    error?: string;
}

export default function Input({ type = 'text', style, value, onChange, placeholder, error }: InputProps) {
    return (
        <div style={{ display: 'flex', flexDirection: 'column'}}>
            <input type={type} style={style} className={`input-component ${error ? 'error' : ''}`} value={value} onChange={onChange} placeholder={placeholder}/>
            {error && <span className="error-message">{error}</span>}
        </div>
    )
}