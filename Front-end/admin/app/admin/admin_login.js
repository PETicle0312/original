import { useRouter } from "expo-router";
import { useState } from "react";
import {
  Alert,
  Image,
  KeyboardAvoidingView,
  Platform,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from "react-native";
import axios from "axios";
import AsyncStorage from "@react-native-async-storage/async-storage";

export const options = { headerShown: false };

export default function LoginScreen() {
  const router = useRouter();
  const [managerId, setManagerId] = useState("");
  const [password, setPassword] = useState("");

  const onLogin = async () => {
    try {
      const response = await axios.post(
        "http://172.18.38.26:8080/api/admin/login",
        {
          adminId: Number(managerId),
          password: password,
        }
      );

      if (response.status === 200) {
        await AsyncStorage.setItem("adminId", managerId);
        Alert.alert("로그인 성공", response.data.adminName + " 님 환영합니다!");
        router.replace("/admin/admin_main");
      } else {
        Alert.alert("로그인 실패", "아이디 또는 비밀번호가 틀렸습니다.");
      }
    } catch (error) {
      console.error("❌ 로그인 실패:", error);

      let errorMessage = "네트워크 또는 서버 오류입니다.";
      if (error.response && typeof error.response.data === "string") {
        errorMessage = error.response.data;
      } else if (error.response && error.response.data?.message) {
        errorMessage = error.response.data.message;
      }

      Alert.alert("로그인 실패", errorMessage);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === "ios" ? "padding" : "height"}
    >
      <View style={styles.logoBox}>
        <Image
          source={require("../../assets/images/logo.png")}
          style={styles.logo}
          resizeMode="contain"
        />
      </View>
      <View style={styles.inputBox}>
        <TextInput
          style={styles.input}
          placeholder="관리자 번호"
          placeholderTextColor="#999"
          value={managerId}
          onChangeText={setManagerId}
          keyboardType="default"
        />
        <TextInput
          style={styles.input}
          placeholder="비밀번호"
          placeholderTextColor="#999"
          value={password}
          onChangeText={setPassword}
          secureTextEntry
        />
      </View>
      <TouchableOpacity style={styles.loginBtn} onPress={onLogin}>
        <Text style={styles.loginBtnText}>로그인</Text>
      </TouchableOpacity>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#F8F6F6",
    alignItems: "center",
    justifyContent: "center",
    paddingHorizontal: 24,
    marginTop: -50,
  },
  logoBox: {
    alignItems: "center",
  },
  logo: {
    width: 110,
    height: 110,
  },
  inputBox: {
    width: "100%",
    alignItems: "center",
    justifyContent: "center",
    marginTop: 30,
  },
  input: {
    width: "100%",
    height: 44,
    borderBottomWidth: 1,
    borderBottomColor: "#ccc",
    backgroundColor: "transparent",
    fontSize: 15,
    marginBottom: 16,
    color: "#333",
    paddingHorizontal: 6,
  },
  loginBtn: {
    width: "100%",
    height: 46,
    backgroundColor: "#fff",
    borderRadius: 8,
    marginTop: 32,
    paddingVertical: 14,
    alignItems: "center",
    borderWidth: 1,
    borderColor: "#D5D5D5",
  },
  loginBtnText: {
    fontSize: 16,
    color: "#818181",
    fontWeight: "bold",
  },
});
